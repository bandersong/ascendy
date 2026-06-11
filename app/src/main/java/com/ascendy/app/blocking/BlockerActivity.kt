package com.ascendy.app.blocking

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ascendy.app.AscendyApp
import com.ascendy.app.ui.components.Mascot
import com.ascendy.app.ui.theme.AscendyTheme
import com.ascendy.app.ui.theme.ThemeVariant
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab

class BlockerActivity : ComponentActivity() {

    // With enableOnBackInvokedCallback=true the legacy onBackPressed() is never called on
    // API 33+ — predictive back would dismiss the blocker unless we register here.
    private var backCallback: OnBackInvokedCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            backCallback = OnBackInvokedCallback { /* swallow back */ }.also {
                onBackInvokedDispatcher.registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_OVERLAY, it
                )
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val app = application as AscendyApp
        setContent {
            val variant by app.themePrefs.variant.collectAsState(initial = ThemeVariant.Kawaii)
            AscendyTheme(variant = variant) {
                BlockerScreen()
            }
        }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            backCallback?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) }
            backCallback = null
        }
        super.onDestroy()
    }

    // Pre-33 devices still route through the legacy path.
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        // swallow back
    }
}

@Composable
private fun BlockerScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                Mascot(locked = true)
            }
            Spacer(Modifier.height(24.dp))
            Text(
                vocab.blockerTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = palette.Ink,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                vocab.blockerBody,
                style = MaterialTheme.typography.bodyLarge,
                color = palette.Smoke,
                textAlign = TextAlign.Center
            )
        }
    }
}
