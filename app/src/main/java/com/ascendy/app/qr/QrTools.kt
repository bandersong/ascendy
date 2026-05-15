package com.ascendy.app.qr

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object QrTools {

    const val PAYLOAD_PREFIX = "ascendy:"

    /** Generate a fresh anchor id and its QR payload. */
    fun newAnchor(): Pair<String, String> {
        val id = UUID.randomUUID().toString()
        return id to "$PAYLOAD_PREFIX$id"
    }

    /** Render a high-contrast QR bitmap for the given payload. */
    fun render(payload: String, sizePx: Int = 768): Bitmap {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 2,
        )
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val w = matrix.width
        val h = matrix.height
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        for (y in 0 until h) {
            for (x in 0 until w) {
                bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    /** Parse a scanned payload; returns the anchor id if it looks like an ascendy QR. */
    fun parseScannedPayload(raw: String): String? {
        val trimmed = raw.trim()
        return if (trimmed.startsWith(PAYLOAD_PREFIX) && trimmed.length > PAYLOAD_PREFIX.length) {
            trimmed.removePrefix(PAYLOAD_PREFIX)
        } else null
    }

    /**
     * Save a QR bitmap to the device gallery (Pictures/Ascendy). Returns the content URI or null
     * on failure. Uses MediaStore on API 29+; pre-29 writes to public Pictures dir.
     */
    fun saveToGallery(context: Context, bitmap: Bitmap, displayName: String): Uri? {
        val fileName = "$displayName.png"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(context, bitmap, fileName)
        } else {
            saveLegacy(context, bitmap, fileName)
        }
    }

    private fun saveViaMediaStore(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Ascendy")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        try {
            resolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            return uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            return null
        }
    }

    @Suppress("DEPRECATION")
    private fun saveLegacy(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val ascendyDir = File(picturesDir, "Ascendy").apply { if (!exists()) mkdirs() }
        val outFile = File(ascendyDir, fileName)
        return try {
            FileOutputStream(outFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(context, context.packageName + ".fileprovider", outFile)
        } catch (_: Exception) { null }
    }

    /** Build a share intent for the saved PNG URI. */
    fun buildShareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
