package com.ascendy.app.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Build
import android.os.Parcelable
import java.security.MessageDigest
import java.util.UUID

object NfcManager {
    private const val MIME = "application/vnd.ascendy.tag"

    fun adapter(activity: Activity): NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    fun enableForegroundDispatch(activity: Activity) {
        val adapter = adapter(activity) ?: return
        val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val intent = Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(activity, 0, intent, pendingFlags)

        val ndefFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            try { addDataType(MIME) } catch (_: IntentFilter.MalformedMimeTypeException) {}
        }
        val techFilter = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        val tagFilter = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)

        adapter.enableForegroundDispatch(
            activity,
            pending,
            arrayOf(ndefFilter, techFilter, tagFilter),
            arrayOf(
                arrayOf(Ndef::class.java.name),
                arrayOf(NdefFormatable::class.java.name)
            )
        )
    }

    fun disableForegroundDispatch(activity: Activity) {
        adapter(activity)?.disableForegroundDispatch(activity)
    }

    @Suppress("DEPRECATION")
    private fun extractTag(intent: Intent): Tag? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            intent.getParcelableExtra<Parcelable>(NfcAdapter.EXTRA_TAG) as? Tag
        }
    }

    /** Returns a stable identifier for the tag (UUID written to it, or hash of UID). */
    fun readTagId(intent: Intent): String? {
        val tag = extractTag(intent) ?: return null
        readNdefId(tag)?.let { return it }
        return uidHash(tag)
    }

    private fun readNdefId(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            val msg: NdefMessage? = ndef.ndefMessage
            msg?.records?.firstOrNull { rec ->
                rec.tnf == NdefRecord.TNF_MIME_MEDIA &&
                    String(rec.type, Charsets.US_ASCII) == MIME
            }?.let { String(it.payload, Charsets.UTF_8) }
        } catch (_: Exception) {
            null
        } finally {
            try { ndef.close() } catch (_: Exception) {}
        }
    }

    /** Try to write a fresh UUID v4 onto the tag. Returns the id we used (NDEF or UID hash). */
    fun pairTag(intent: Intent): String? {
        val tag = extractTag(intent) ?: return null
        // already paired?
        readNdefId(tag)?.let { return it }

        val newId = UUID.randomUUID().toString()
        val record = NdefRecord.createMime(MIME, newId.toByteArray(Charsets.UTF_8))
        val message = NdefMessage(arrayOf(record))

        // Try writable Ndef first
        Ndef.get(tag)?.let { ndef ->
            try {
                ndef.connect()
                if (ndef.isWritable) {
                    ndef.writeNdefMessage(message)
                    return newId
                }
            } catch (_: Exception) {
                // fall through
            } finally {
                try { ndef.close() } catch (_: Exception) {}
            }
        }

        // Try formatable
        NdefFormatable.get(tag)?.let { fmt ->
            try {
                fmt.connect()
                fmt.format(message)
                return newId
            } catch (_: Exception) {
                // fall through
            } finally {
                try { fmt.close() } catch (_: Exception) {}
            }
        }

        // Fallback: derive id from UID
        return uidHash(tag)
    }

    private fun uidHash(tag: Tag): String? {
        val id = tag.id ?: return null
        if (id.isEmpty()) return null
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(id)
        return "uid:" + hash.joinToString("") { "%02x".format(it) }.take(32)
    }
}
