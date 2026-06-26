package com.ascendy.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.ascendy.app.ui.components.PageColumn
import com.ascendy.app.ui.components.ScreenHeader
import com.ascendy.app.ui.components.SoftCard
import com.ascendy.app.ui.theme.Space
import com.ascendy.app.ui.theme.VSpace
import com.ascendy.app.ui.theme.palette
import com.ascendy.app.ui.theme.vocab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    PageColumn {
        ScreenHeader(title = vocab.aboutTitle, onBack = onBack)

        VSpace(Space.lg)

        SoftCard(modifier = Modifier.fillMaxWidth(), color = palette.Cloud) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    Mascot(locked = false)
                }
                VSpace(Space.sm)
                Text(
                    vocab.appTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = palette.Ink,
                    textAlign = TextAlign.Center
                )
                VSpace(Space.xs)
                Text(
                    vocab.aboutVersionFmt.format(BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.Smoke,
                    textAlign = TextAlign.Center
                )
                VSpace(Space.md)
                Text(
                    vocab.aboutTagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.Smoke,
                    textAlign = TextAlign.Center
                )
            }
        }

        VSpace(Space.lg)

        LinkRow(
            label = vocab.aboutLinkDonate,
            url = "https://ko-fi.com/bandersong",
            emphasized = true
        )
        VSpace(Space.sm)
        LinkRow(
            label = vocab.aboutLinkSource,
            url = "https://github.com/bandersong/ascendy"
        )
        VSpace(Space.sm)
        LinkRow(
            label = vocab.aboutLinkPrivacy,
            url = "https://bandersong.github.io/ascendy/privacy.html"
        )
        VSpace(Space.sm)
        LinkRow(
            label = vocab.aboutLinkReleases,
            url = "https://github.com/bandersong/ascendy/releases"
        )

        VSpace(Space.xxl)

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
        border = if (emphasized) null else androidx.compose.foundation.BorderStroke(1.dp, palette.Mist),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = Space.xl, vertical = Space.lg),
            style = MaterialTheme.typography.titleMedium,
            color = if (emphasized) palette.onPetal else palette.Ink
        )
    }
}
