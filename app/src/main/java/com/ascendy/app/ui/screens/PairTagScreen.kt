package com.ascendy.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import com.ascendy.app.data.Blocklist
import com.ascendy.app.data.BoundTag
import com.ascendy.app.nfc.NfcFailure
import com.ascendy.app.ui.components.EmptyState
import com.ascendy.app.ui.components.Mascot
import com.ascendy.app.ui.components.PageColumn
import com.ascendy.app.ui.components.ScreenHeader
import com.ascendy.app.ui.components.SectionLabel
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.HSpace
import com.ascendy.app.ui.theme.Space
import com.ascendy.app.ui.theme.VSpace
import com.ascendy.app.ui.theme.nfcFailureText
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab

@Composable
fun PairTagScreen(
    waiting: Boolean,
    detectedTagId: String?,
    pairFailure: NfcFailure?,
    knownTags: List<BoundTag>,
    lists: List<Blocklist>,
    nfcSupported: Boolean,
    nfcEnabled: Boolean,
    onOpenNfcSettings: () -> Unit,
    onStartPairing: () -> Unit,
    onCancelPairing: (reason: NfcFailure?) -> Unit,
    onSavePairing: (nickname: String) -> Unit,
    onDeleteTag: (BoundTag) -> Unit,
    onAssignList: (BoundTag, Long?) -> Unit,
    onSaveQrAnchor: (anchorId: String, nickname: String) -> Unit,
    onSaveQrToGallery: (anchorId: String) -> Unit,
    onShareQr: (anchorId: String) -> Unit,
    onBack: () -> Unit,
) {
    // Saveable, not remember: a fold/unfold recreates the Activity, and typing a tag nickname
    // is exactly when a Flip gets closed. Same for the pending QR anchor.
    var nickname by rememberSaveable { mutableStateOf("") }
    var qrAnchorId by rememberSaveable { mutableStateOf<String?>(null) }
    var qrNickname by rememberSaveable { mutableStateOf("") }

    // Pairing must not wait forever (e.g. NFC turned off mid-wait, broken antenna) — auto-cancel
    // after 2 minutes so the screen never becomes a dead end. It says WHY it gave up: a silent
    // return to idle reads as "I pressed the button and nothing happened".
    androidx.compose.runtime.LaunchedEffect(waiting) {
        if (waiting) {
            kotlinx.coroutines.delay(120_000L)
            onCancelPairing(NfcFailure.TimedOut)
        }
    }

    // The screen timeout is shorter than the pairing wait on plenty of phones (30s on the Z Flip 7).
    // When the screen sleeps the activity pauses, which tears down NFC foreground dispatch — so
    // pairing could not succeed for anyone slower than their own display timeout. Hold the screen
    // awake only while we're actually waiting for a tag, never for the whole app.
    val view = LocalView.current
    DisposableEffect(waiting) {
        view.keepScreenOn = waiting
        onDispose { view.keepScreenOn = false }
    }

    PageColumn {
        ScreenHeader(title = vocab.tagsTitle, onBack = onBack)

        VSpace(Space.sm)

        SoftCard(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                    Mascot(locked = waiting)
                }
                VSpace(Space.sm)
                // Why the last attempt didn't work. Sits above the controls so the retry button is
                // right under the explanation.
                if (pairFailure != null && detectedTagId == null) {
                    Text(
                        vocab.nfcFailureText(pairFailure),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    VSpace(Space.sm)
                }
                when {
                    !waiting && detectedTagId == null -> {
                        // NFC off looks identical to NFC on here unless we check: the idle screen
                        // would otherwise invite a tap that the radio cannot possibly receive.
                        val nfcBlocked = nfcSupported && !nfcEnabled
                        Text(
                            when {
                                !nfcSupported -> vocab.nfcUnsupportedBody
                                nfcBlocked -> vocab.nfcOffBody
                                else -> vocab.tagsIntro
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.Smoke
                        )
                        VSpace(Space.md)
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                            if (nfcBlocked) {
                                Button(onClick = onOpenNfcSettings) { Text(vocab.nfcOffAction) }
                            } else if (nfcSupported) {
                                Button(onClick = onStartPairing) { Text(vocab.tagsStartPairing) }
                            }
                            TextButton(onClick = {
                                qrAnchorId = java.util.UUID.randomUUID().toString()
                                qrNickname = ""
                            }) { Text(vocab.qrGenerateButton) }
                        }
                    }
                    waiting && detectedTagId == null -> {
                        if (nfcEnabled) {
                            Text(
                                vocab.tagsWaiting,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.Ink
                            )
                        } else {
                            Text(
                                vocab.nfcOffBody,
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.Ink
                            )
                            VSpace(Space.sm)
                            Button(onClick = onOpenNfcSettings) { Text(vocab.nfcOffAction) }
                        }
                        VSpace(Space.sm)
                        TextButton(onClick = { onCancelPairing(null) }) { Text(vocab.tagsCancel) }
                    }
                    detectedTagId != null -> {
                        Text(vocab.tagsFound, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                        VSpace(Space.md)
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nickname = it },
                            label = { Text(vocab.tagsNicknameLabel) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        VSpace(Space.md)
                        Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                            TextButton(onClick = { onCancelPairing(null) }) { Text(vocab.tagsCancel) }
                            // enabled is driven by the same condition the click used to check, so a
                            // button that will do nothing also LOOKS like it will do nothing.
                            Button(
                                enabled = nickname.isNotBlank(),
                                onClick = { onSavePairing(nickname.trim()) }
                            ) { Text(vocab.tagsSave) }
                        }
                    }
                }
            }
        }

        VSpace(Space.lg)

        SectionLabel(vocab.tagsListHeader)
        VSpace(Space.sm)

        if (knownTags.isEmpty()) {
            EmptyState(vocab.tagsEmpty)
        } else {
            knownTags.forEach { tag ->
                SoftCard(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(tag.nickname, style = MaterialTheme.typography.titleMedium, color = palette.Ink)
                                Text(
                                    tag.tagId.take(10) + "…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.Smoke
                                )
                            }
                            TextButton(onClick = { onDeleteTag(tag) }) { Text(vocab.tagsRemove) }
                        }
                        if (lists.size > 1 || tag.listId != null) {
                            VSpace(Space.sm)
                            Text(vocab.tagListPickerLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.Smoke)
                            VSpace(Space.xs)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(Space.sm),
                                verticalArrangement = Arrangement.spacedBy(Space.sm),
                            ) {
                                ListChip(
                                    text = vocab.tagListPickerDefault,
                                    selected = tag.listId == null,
                                    onClick = { onAssignList(tag, null) }
                                )
                                lists.forEach { l ->
                                    ListChip(
                                        text = l.name,
                                        selected = tag.listId == l.id,
                                        onClick = { onAssignList(tag, l.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                VSpace(Space.sm)
            }
        }
    }

    val qrIdSnapshot = qrAnchorId
    if (qrIdSnapshot != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { qrAnchorId = null },
            title = { Text(vocab.qrGeneratedTitle) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val bmp = remember(qrIdSnapshot) {
                        com.ascendy.app.qr.QrTools.render(
                            com.ascendy.app.qr.QrTools.PAYLOAD_PREFIX + qrIdSnapshot,
                            sizePx = 600
                        )
                    }
                    androidx.compose.foundation.Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = vocab.qrGeneratedTitle,
                        modifier = Modifier.size(220.dp)
                    )
                    VSpace(Space.sm)
                    Text(vocab.qrInstructions,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.Smoke)
                    VSpace(Space.sm)
                    OutlinedTextField(
                        value = qrNickname,
                        onValueChange = { qrNickname = it },
                        label = { Text(vocab.qrNicknameHint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    VSpace(Space.sm)
                    // Exporting registers the anchor first — a printed QR that was never saved
                    // could not end sessions, which defeats the whole point of printing it.
                    val defaultNickname = vocab.qrDefaultNickname
                    val registerAnchor = {
                        onSaveQrAnchor(qrIdSnapshot, qrNickname.trim().ifBlank { defaultNickname })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                        TextButton(onClick = {
                            registerAnchor()
                            onSaveQrToGallery(qrIdSnapshot)
                        }) { Text(vocab.qrSaveToGallery) }
                        TextButton(onClick = {
                            registerAnchor()
                            onShareQr(qrIdSnapshot)
                        }) { Text(vocab.qrShare) }
                    }
                    VSpace(Space.xs)
                    Text(vocab.qrExportNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.Smoke)
                }
            },
            confirmButton = {
                // Same fix as the NFC Save button: an accent-filled button that silently does
                // nothing when Name is empty is worse than one that reads as disabled.
                Button(
                    enabled = qrNickname.isNotBlank(),
                    onClick = {
                        onSaveQrAnchor(qrIdSnapshot, qrNickname.trim())
                        qrAnchorId = null
                    }
                ) { Text(vocab.qrSaveAnchor) }
            },
            dismissButton = {
                TextButton(onClick = { qrAnchorId = null }) { Text(vocab.tagsCancel) }
            }
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ListChip(text: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        color = if (selected) palette.Petal else palette.Cloud,
        shape = MaterialTheme.shapes.small,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, palette.Mist),
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = Space.md, vertical = Space.sm),
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) palette.onPetal else palette.Ink
        )
    }
}
