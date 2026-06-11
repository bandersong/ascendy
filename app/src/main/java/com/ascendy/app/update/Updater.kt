package com.ascendy.app.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.ascendy.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Lightweight in-app updater that queries the GitHub Releases API and triggers a system install
 * of the downloaded APK. Pure HttpURLConnection + JSONObject — no HTTP libraries pulled in.
 */
object Updater {

    private const val USER_AGENT = "ascendy-app"
    private const val APK_ASSET_NAME = "ascendy-debug.apk"

    /** Parse a release tag like "v47" or "47" into its integer versionCode, or null if malformed. */
    internal fun parseLatestVersionCode(tag: String): Int? = tag.removePrefix("v").trim().toIntOrNull()

    /** An update is offered only when the published code is strictly greater than the installed one. */
    internal fun isUpdateAvailable(latestCode: Int, currentCode: Int): Boolean = latestCode > currentCode

    sealed class CheckResult {
        data class Available(
            val latestVersionCode: Int,
            val downloadUrl: String,
            val releaseName: String,
            val notes: String,
            val sizeBytes: Long,
        ) : CheckResult()
        object UpToDate : CheckResult()
        data class Error(val message: String) : CheckResult()
    }

    sealed class DownloadProgress {
        data class Progress(val bytesRead: Long, val total: Long) : DownloadProgress()
        data class Done(val file: File) : DownloadProgress()
        data class Error(val message: String) : DownloadProgress()
    }

    suspend fun check(): CheckResult = withDispatchers {
        val repo = BuildConfig.RELEASE_REPO
        val url = URL("https://api.github.com/repos/$repo/releases/latest")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", USER_AGENT)
            connectTimeout = 10_000
            readTimeout = 10_000
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) return@withDispatchers CheckResult.Error("HTTP $code")
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val rawTag = json.optString("tag_name")
            val latestCode = parseLatestVersionCode(rawTag)
                ?: return@withDispatchers CheckResult.Error("bad tag: $rawTag")
            if (!isUpdateAvailable(latestCode, BuildConfig.VERSION_CODE)) return@withDispatchers CheckResult.UpToDate

            val assets = json.optJSONArray("assets") ?: return@withDispatchers CheckResult.Error("no assets")
            var dl: String? = null
            var sz = 0L
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                if (a.optString("name") == APK_ASSET_NAME) {
                    dl = a.optString("browser_download_url")
                    sz = a.optLong("size")
                    break
                }
            }
            if (dl.isNullOrEmpty()) return@withDispatchers CheckResult.Error("no apk asset")
            CheckResult.Available(
                latestVersionCode = latestCode,
                downloadUrl = dl,
                releaseName = json.optString("name").ifEmpty { "Build $latestCode" },
                notes = json.optString("body"),
                sizeBytes = sz,
            )
        } catch (e: Exception) {
            CheckResult.Error(e.message ?: "check failed")
        } finally {
            conn.disconnect()
        }
    }

    fun download(context: Context, url: String): Flow<DownloadProgress> = flow {
        val outDir = File(context.cacheDir, "updates").apply { mkdirs() }
        // wipe stale apks before downloading the new one
        outDir.listFiles()?.forEach { it.delete() }
        val outFile = File(outDir, APK_ASSET_NAME)

        var conn: HttpURLConnection? = null
        try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", USER_AGENT)
                connectTimeout = 15_000
                readTimeout = 60_000
            }
            val code = conn.responseCode
            if (code !in 200..299) {
                emit(DownloadProgress.Error("HTTP $code"))
                return@flow
            }
            val total = conn.contentLengthLong.coerceAtLeast(0L)
            conn.inputStream.use { input ->
                outFile.outputStream().use { output ->
                    val buf = ByteArray(64 * 1024)
                    var read = input.read(buf)
                    var sum = 0L
                    var lastEmitted = 0L
                    while (read > 0) {
                        output.write(buf, 0, read)
                        sum += read
                        // throttle progress emissions to avoid flooding the UI
                        if (sum - lastEmitted > 64 * 1024 || sum == total) {
                            emit(DownloadProgress.Progress(sum, total))
                            lastEmitted = sum
                        }
                        read = input.read(buf)
                    }
                }
            }
            // SECURITY: never hand an unverified APK to the system installer. The release endpoint is
            // attacker-reachable and the repo's debug keystore is public, so Android's own signature
            // check is not a sufficient gate. Pin the install to this app's own signing certificate.
            val rejection = verifyApk(context, outFile)
            if (rejection != null) {
                outFile.delete()
                emit(DownloadProgress.Error("update rejected: $rejection"))
                return@flow
            }
            emit(DownloadProgress.Done(outFile))
        } catch (e: Exception) {
            emit(DownloadProgress.Error(e.message ?: "download failed"))
        } finally {
            conn?.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Launches the system package installer for [apk] — but only after re-verifying it (defense in
     * depth; the download path already verified before reaching this state). Returns null on success,
     * or a human-readable rejection reason if the APK failed verification, in which case nothing is
     * launched.
     */
    fun launchInstall(context: Context, apk: File): String? {
        verifyApk(context, apk)?.let { return "update rejected: $it" }
        val uri: Uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", apk
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return null
    }

    /**
     * Verifies a downloaded APK before it is offered to the system installer.
     *
     * A compromised or MITM'd release — or anyone able to publish to the releases endpoint — could
     * serve an arbitrary APK, and because the debug keystore is committed to this repo, Android's
     * install-time signature check alone cannot be trusted. We therefore pin the install to this
     * app's own identity: the APK must declare our package name, be a strictly newer version, and be
     * signed by exactly the same certificate(s) as the currently-running app.
     *
     * @return null when the APK is safe to install, or a human-readable rejection reason otherwise.
     */
    fun verifyApk(context: Context, apk: File): String? {
        if (!apk.isFile || apk.length() == 0L) return "downloaded file is missing or empty"
        val pm = context.packageManager

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION") PackageManager.GET_SIGNATURES
        }

        val apkInfo = pm.getPackageArchiveInfo(apk.absolutePath, flags)
            ?: return "could not read the downloaded APK"

        if (apkInfo.packageName != context.packageName) {
            return "package mismatch (${apkInfo.packageName} != ${context.packageName})"
        }

        // Reject downgrades and replays: a self-update must be strictly newer than what is installed.
        val apkCode = apkInfo.longVersionCodeCompat()
        if (apkCode <= BuildConfig.VERSION_CODE.toLong()) {
            return "version $apkCode is not newer than installed ${BuildConfig.VERSION_CODE}"
        }

        val installed = try {
            pm.getPackageInfo(context.packageName, flags)
        } catch (e: Exception) {
            return "could not read the installed app signature"
        }

        val apkSigners = signerSha256(apkInfo)
        val installedSigners = signerSha256(installed)
        if (apkSigners.isEmpty() || installedSigners.isEmpty()) {
            return "APK is unsigned or its signature could not be read"
        }
        if (apkSigners != installedSigners) {
            return "signing certificate does not match this app's key"
        }
        return null
    }

    /** SHA-256 digests (lowercase hex) of the current signing certificate(s) in [info]. */
    private fun signerSha256(info: PackageInfo): Set<String> {
        val sigs: Array<Signature>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo?.apkContentsSigners
        } else {
            @Suppress("DEPRECATION") info.signatures
        }
        return sigs.orEmpty().mapNotNull { sig ->
            try {
                MessageDigest.getInstance("SHA-256")
                    .digest(sig.toByteArray())
                    .joinToString("") { "%02x".format(it.toInt() and 0xFF) }
            } catch (e: Exception) {
                null
            }
        }.toSet()
    }

    @Suppress("DEPRECATION")
    private fun PackageInfo.longVersionCodeCompat(): Long =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) longVersionCode else versionCode.toLong()

    fun canRequestInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true
    }

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(
                android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:" + context.packageName)
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    private suspend fun <T> withDispatchers(block: suspend () -> T): T =
        kotlinx.coroutines.withContext(Dispatchers.IO) { block() }
}
