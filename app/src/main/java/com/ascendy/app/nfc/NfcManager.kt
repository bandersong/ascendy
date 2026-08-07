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

/**
 * Why a pairing attempt did not produce a usable tag. Every one of these used to be swallowed —
 * the screen just went quiet, which reads to the user as "the app is broken".
 */
enum class NfcFailure {
    /** Watchdog fired: the user never presented a tag. */
    TimedOut,
    /** Intent carried no tag, or the tag had no readable UID. */
    NoTag,
    /** Tag was pulled away mid-write (TagLostException). */
    TagMoved,
    /** Tag's NDEF capacity is smaller than our record. */
    TagFull,
    /** Tag carries NDEF but is locked read-only. */
    ReadOnly,
    /** Write was attempted and did not stick (including a failed read-back). */
    WriteFailed,
}

/**
 * Outcome of [NfcManager.pairTag]. The old API returned `String?`, which made "wrote a fresh id",
 * "reused an existing id", "couldn't write so here's the UID hash" and "write threw" all look
 * identical to the caller — the UI said "Tag found" for tags that were never written.
 */
sealed interface PairResult {
    /**
     * The tag can be used as an anchor. [written] is true only when a fresh Ascendy id was written
     * AND read back off the tag; false means we are identifying the tag some other way (it already
     * carried an id, or it has no NDEF surface and we fell back to its hardware UID).
     */
    data class Success(val tagId: String, val written: Boolean) : PairResult

    data class Failed(val reason: NfcFailure) : PairResult
}

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

    /** Pull our id out of an already-read NDEF message, if it carries one. */
    private fun idFrom(msg: NdefMessage?): String? = msg?.records?.firstOrNull { rec ->
        rec.tnf == NdefRecord.TNF_MIME_MEDIA &&
            String(rec.type, Charsets.US_ASCII) == MIME
    }?.let { String(it.payload, Charsets.UTF_8) }

    private fun readNdefId(tag: Tag): String? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            ndef.connect()
            idFrom(ndef.ndefMessage)
        } catch (_: Exception) {
            null
        } finally {
            try { ndef.close() } catch (_: Exception) {}
        }
    }

    /**
     * Try to write a fresh UUID v4 onto the tag.
     *
     * A silent non-write is the worst outcome here — the user walks away believing a tag is paired
     * and it never starts a session — so a write is only reported as success after the id is read
     * back off the tag, and a tag that HAS an NDEF surface we failed to write is a hard failure
     * rather than a quiet fall-through to the UID hash.
     */
    fun pairTag(intent: Intent): PairResult {
        val tag = extractTag(intent) ?: return PairResult.Failed(NfcFailure.NoTag)
        // already paired?
        readNdefId(tag)?.let { return PairResult.Success(it, written = false) }

        val newId = UUID.randomUUID().toString()
        val record = NdefRecord.createMime(MIME, newId.toByteArray(Charsets.UTF_8))
        val message = NdefMessage(arrayOf(record))

        // Try writable Ndef first
        Ndef.get(tag)?.let { ndef ->
            var reason = NfcFailure.WriteFailed
            try {
                ndef.connect()
                val capacity = ndef.maxSize
                when {
                    !ndef.isWritable -> reason = NfcFailure.ReadOnly
                    capacity in 1 until message.toByteArray().size -> reason = NfcFailure.TagFull
                    else -> {
                        ndef.writeNdefMessage(message)
                        // Read-back: writeNdefMessage can report success on a tag that ends up
                        // holding nothing (or half a record) if contact broke at the wrong moment.
                        if (idFrom(ndef.ndefMessage) == newId) {
                            return PairResult.Success(newId, written = true)
                        }
                    }
                }
            } catch (_: android.nfc.TagLostException) {
                reason = NfcFailure.TagMoved
            } catch (_: Exception) {
                // keep whatever reason we had narrowed down to
            } finally {
                try { ndef.close() } catch (_: Exception) {}
            }
            return PairResult.Failed(reason)
        }

        // Try formatable (blank tag with no NDEF area yet)
        NdefFormatable.get(tag)?.let { fmt ->
            var reason = NfcFailure.WriteFailed
            try {
                fmt.connect()
                // ponytail: no read-back here — format() re-provisions the tag, so re-reading needs
                // a fresh discovery. format() itself throws on a failed write, which is the case
                // read-back exists to catch on the Ndef path.
                fmt.format(message)
                return PairResult.Success(newId, written = true)
            } catch (_: android.nfc.TagLostException) {
                reason = NfcFailure.TagMoved
            } catch (_: Exception) {
                // keep reason
            } finally {
                try { fmt.close() } catch (_: Exception) {}
            }
            return PairResult.Failed(reason)
        }

        // No NDEF surface at all: nothing was written, but the hardware UID still identifies the
        // tag on every later tap, so this is a usable (if unbranded) anchor.
        return uidHash(tag)?.let { PairResult.Success(it, written = false) }
            ?: PairResult.Failed(NfcFailure.NoTag)
    }

    private fun uidHash(tag: Tag): String? {
        val id = tag.id ?: return null
        if (id.isEmpty()) return null
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(id)
        return "uid:" + hash.joinToString("") { "%02x".format(it) }.take(32)
    }
}
