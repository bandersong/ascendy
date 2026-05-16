package com.ascendy.app.update

import android.content.Context
import android.content.Intent
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

/**
 * Lightweight in-app updater that queries the GitHub Releases API and triggers a system install
 * of the downloaded APK. Pure HttpURLConnection + JSONObject — no HTTP libraries pulled in.
 */
object Updater {

    private const val USER_AGENT = "ascendy-app"
    private const val APK_ASSET_NAME = "ascendy-debug.apk"

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
            val tag = json.optString("tag_name").removePrefix("v")
            val latestCode = tag.toIntOrNull()
                ?: return@withDispatchers CheckResult.Error("bad tag: $tag")
            if (latestCode <= BuildConfig.VERSION_CODE) return@withDispatchers CheckResult.UpToDate

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
            emit(DownloadProgress.Done(outFile))
        } catch (e: Exception) {
            emit(DownloadProgress.Error(e.message ?: "download failed"))
        } finally {
            conn?.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    fun launchInstall(context: Context, apk: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", apk
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

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
