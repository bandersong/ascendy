package com.ascendy.app.service

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.ascendy.app.AscendyApp
import com.ascendy.app.R
import com.ascendy.app.blocking.BlockState
import com.ascendy.app.blocking.ManualEndResult
import com.ascendy.app.blocking.SessionController
import com.ascendy.app.ui.theme.vocabFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Quick Settings tile. Pull down the notification shade, tap to toggle a manual focus session.
 * Tile state mirrors [BlockState.active] in real time while the panel is open.
 */
class AscendyTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var collectorJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        render()
        collectorJob = scope.launch {
            BlockState.active.collect { render() }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        collectorJob?.cancel()
        collectorJob = null
    }

    override fun onClick() {
        super.onClick()
        val app = applicationContext as AscendyApp
        val controller = SessionController(applicationContext, app.repo, app.themePrefs)
        val vocab = vocabFor(app.currentVariant)
        scope.launch {
            val result = controller.toggleManual()
            val msg = when (result) {
                ManualEndResult.BlockedStrict -> vocab.strictManualBlockedToast
                ManualEndResult.Ended -> vocab.toastManualEnded
                ManualEndResult.NoSession -> vocab.toastManualStarted
            }
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(applicationContext, msg, android.widget.Toast.LENGTH_SHORT).show()
            }
            render()
        }
    }

    private fun render() {
        val tile = qsTile ?: return
        val active = BlockState.isActive()
        val app = applicationContext as AscendyApp
        val vocab = vocabFor(app.currentVariant)
        tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Ascendy"
        tile.contentDescription = vocab.appTitle
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            tile.subtitle = if (active) vocab.statusFocusing else vocab.statusReady
        }
        tile.icon = Icon.createWithResource(this, R.drawable.ic_logo)
        tile.updateTile()
    }
}
