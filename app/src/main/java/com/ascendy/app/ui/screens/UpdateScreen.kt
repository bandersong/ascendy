package com.ascendy.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ascendy.app.BuildConfig
import com.ascendy.app.ui.components.PageColumn
import com.ascendy.app.ui.components.ScreenHeader
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.Space
import com.ascendy.app.ui.theme.VSpace
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab
import com.ascendy.app.update.Updater
import kotlinx.coroutines.launch

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class Available(val info: Updater.CheckResult.Available) : UpdateState()
    object UpToDate : UpdateState()
    data class Error(val message: String) : UpdateState()
    data class Downloading(val bytesRead: Long, val total: Long) : UpdateState()
    data class Ready(val file: java.io.File) : UpdateState()
}

@Composable
fun UpdateScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf<UpdateState>(UpdateState.Idle) }

    LaunchedEffect(Unit) {
        state = UpdateState.Checking
        state = when (val r = Updater.check()) {
            is Updater.CheckResult.Available -> UpdateState.Available(r)
            Updater.CheckResult.UpToDate -> UpdateState.UpToDate
            is Updater.CheckResult.Error -> UpdateState.Error(r.message)
        }
    }

    PageColumn {
        ScreenHeader(title = vocab.updateTitle, onBack = onBack)
        VSpace(Space.sm)
        Text(
            vocab.updateCurrentFmt.format(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
            style = MaterialTheme.typography.bodyMedium,
            color = palette.Smoke
        )
        VSpace(Space.lg)

        SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Cloud) {
            when (val s = state) {
                UpdateState.Idle, UpdateState.Checking -> {
                    Text(vocab.updateChecking,
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.Ink)
                    VSpace(Space.sm)
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                UpdateState.UpToDate -> {
                    Text(vocab.updateUpToDate,
                        style = MaterialTheme.typography.titleMedium,
                        color = palette.Ink)
                }
                is UpdateState.Available -> {
                    Column {
                        Text(s.info.releaseName,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.Ink)
                        VSpace(Space.xs)
                        Text(
                            vocab.updateAvailableFmt.format(s.info.latestVersionCode, formatBytes(s.info.sizeBytes)),
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.Smoke
                        )
                        if (s.info.notes.isNotBlank()) {
                            VSpace(Space.sm)
                            Text(s.info.notes.take(500),
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.Smoke)
                        }
                        VSpace(Space.md)
                        Button(onClick = {
                            scope.launch {
                                state = UpdateState.Downloading(0, s.info.sizeBytes)
                                Updater.download(context, s.info.downloadUrl).collect { p ->
                                    state = when (p) {
                                        is Updater.DownloadProgress.Progress ->
                                            UpdateState.Downloading(p.bytesRead, p.total)
                                        is Updater.DownloadProgress.Done ->
                                            UpdateState.Ready(p.file)
                                        is Updater.DownloadProgress.Error ->
                                            UpdateState.Error(p.message)
                                    }
                                }
                            }
                        }) { Text(vocab.updateDownload) }
                    }
                }
                is UpdateState.Downloading -> {
                    Column {
                        val pct = if (s.total > 0) (s.bytesRead.toFloat() / s.total) else 0f
                        Text(vocab.updateDownloadingFmt.format((pct * 100).toInt()),
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.Ink)
                        VSpace(Space.sm)
                        if (s.total > 0) LinearProgressIndicator(progress = { pct }, modifier = Modifier.fillMaxWidth())
                        else LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        VSpace(Space.xs)
                        Text(
                            "${formatBytes(s.bytesRead)} / ${formatBytes(s.total)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.Smoke
                        )
                    }
                }
                is UpdateState.Ready -> {
                    Column {
                        Text(vocab.updateReady,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.Ink)
                        VSpace(Space.sm)
                        if (!Updater.canRequestInstalls(context)) {
                            Text(vocab.updateNeedsInstallPerm,
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.Smoke)
                            VSpace(Space.sm)
                            TextButton(onClick = { Updater.openInstallPermissionSettings(context) }) {
                                Text(vocab.updateGrantInstallPerm)
                            }
                        } else {
                            Button(onClick = {
                                val rejection = Updater.launchInstall(context, s.file)
                                if (rejection != null) state = UpdateState.Error(rejection)
                            }) {
                                Text(vocab.updateInstall)
                            }
                        }
                    }
                }
                is UpdateState.Error -> {
                    Column {
                        Text(vocab.updateError,
                            style = MaterialTheme.typography.titleMedium,
                            color = palette.Ink)
                        VSpace(Space.xs)
                        Text(s.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = palette.Smoke)
                        VSpace(Space.sm)
                        TextButton(onClick = {
                            scope.launch {
                                state = UpdateState.Checking
                                state = when (val r = Updater.check()) {
                                    is Updater.CheckResult.Available -> UpdateState.Available(r)
                                    Updater.CheckResult.UpToDate -> UpdateState.UpToDate
                                    is Updater.CheckResult.Error -> UpdateState.Error(r.message)
                                }
                            }
                        }) { Text(vocab.updateRetry) }
                    }
                }
            }
        }
    }
}

private fun formatBytes(b: Long): String = when {
    b <= 0 -> "—"
    b < 1024 -> "${b}B"
    b < 1024 * 1024 -> "${b / 1024}KB"
    else -> "%.1fMB".format(b / (1024.0 * 1024.0))
}
