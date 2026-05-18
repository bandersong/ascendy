package com.ascendy.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ascendy.app.BuildConfig
import com.ascendy.app.ui.components.Mascot
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val insets = WindowInsets.systemBars.asPaddingValues()
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(top = insets.calculateTopPadding(),
                     bottom = insets.calculateBottomPadding(),
                     start = 16.dp, end = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "back", tint = palette.Ink)
            }
            Text(vocab.aboutTitle, style = MaterialTheme.typography.headlineMedium, color = palette.Ink)
        }

        Spacer(Modifier.height(16.dp))

        SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Cloud) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Mascot(locked = false)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    vocab.appTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.Ink,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    vocab.aboutVersionFmt.format(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.Smoke,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    vocab.aboutTagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.Smoke,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        LinkRow(
            label = vocab.aboutLinkDonate,
            url = "https://ko-fi.com/bandersong",
            emphasized = true
        )
        Spacer(Modifier.height(8.dp))
        LinkRow(
            label = vocab.aboutLinkSource,
            url = "https://github.com/bandersong/ascendy"
        )
        Spacer(Modifier.height(8.dp))
        LinkRow(
            label = vocab.aboutLinkPrivacy,
            url = "https://bandersong.github.io/ascendy/privacy.html"
        )
        Spacer(Modifier.height(8.dp))
        LinkRow(
            label = vocab.aboutLinkReleases,
            url = "https://github.com/bandersong/ascendy/releases"
        )

        Spacer(Modifier.height(24.dp))

        Text(
            vocab.aboutMadeWith,
            style = MaterialTheme.typography.bodySmall,
            color = palette.Smoke,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinkRow(label: String, url: String, emphasized: Boolean = false) {
    val context = LocalContext.current
    Surface(
        onClick = {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        },
        color = if (emphasized) palette.Petal else MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            style = MaterialTheme.typography.titleMedium,
            color = palette.Ink
        )
    }
}
