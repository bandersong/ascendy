package com.ascendy.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ascendy.app.ui.theme.AscendyColors
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Mascot(locked: Boolean, modifier: Modifier = Modifier.fillMaxWidth()) {
    val transition = rememberInfiniteTransition(label = "mascot")
    val bob by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f + bob
            val radius = size.minDimension * 0.32f
            if (locked) drawMoon(cx, cy, radius) else drawStar(cx, cy, radius)
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMoon(
    cx: Float, cy: Float, r: Float
) {
    val body = AscendyColors.Lilac
    val face = AscendyColors.Ink
    val cheek = AscendyColors.Petal

    val outer = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(cx - r, cy - r), Size(r * 2, r * 2))) }
    val cut = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(cx - r * 0.4f, cy - r), Size(r * 2, r * 2))) }
    val crescent = Path().apply { op(outer, cut, PathOperation.Difference) }

    drawPath(crescent, color = body)
    // closed eye
    drawArc(
        color = face,
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.55f, cy - r * 0.2f),
        size = Size(r * 0.45f, r * 0.30f),
        style = Stroke(width = r * 0.08f)
    )
    // cheek
    drawCircle(color = cheek, radius = r * 0.12f, center = Offset(cx - r * 0.65f, cy + r * 0.15f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawStar(
    cx: Float, cy: Float, r: Float
) {
    val body = AscendyColors.Petal
    val face = AscendyColors.Ink
    val cheek = AscendyColors.Lilac
    val shine = AscendyColors.Cream

    val petals = 8
    val outer = r * 1.05f
    val inner = r * 0.72f
    val path = Path()
    for (i in 0 until petals * 2) {
        val angle = (Math.PI * i / petals).toFloat() - (Math.PI / 2).toFloat()
        val rad = if (i % 2 == 0) outer else inner
        val x = cx + rad * cos(angle)
        val y = cy + rad * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = body)

    // big round eyes (kawaii style — way bigger than before)
    val eyeR = r * 0.12f
    val eyeY = cy - r * 0.05f
    drawCircle(color = face, radius = eyeR, center = Offset(cx - r * 0.24f, eyeY))
    drawCircle(color = face, radius = eyeR, center = Offset(cx + r * 0.24f, eyeY))
    // sparkle highlights
    drawCircle(color = shine, radius = eyeR * 0.35f, center = Offset(cx - r * 0.21f, eyeY - eyeR * 0.35f))
    drawCircle(color = shine, radius = eyeR * 0.35f, center = Offset(cx + r * 0.27f, eyeY - eyeR * 0.35f))

    // tiny smile
    drawArc(
        color = face,
        startAngle = 25f,
        sweepAngle = 130f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.16f, cy + r * 0.10f),
        size = Size(r * 0.32f, r * 0.20f),
        style = Stroke(width = r * 0.07f)
    )
    // blush cheeks
    drawCircle(color = cheek, radius = r * 0.10f, center = Offset(cx - r * 0.46f, cy + r * 0.18f))
    drawCircle(color = cheek, radius = r * 0.10f, center = Offset(cx + r * 0.46f, cy + r * 0.18f))
}

/** A tiny static mascot for inline use (no animation). */
@Composable
fun MiniMascot(locked: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension * 0.38f
        if (locked) drawMoon(cx, cy, radius) else drawStar(cx, cy, radius)
    }
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = color,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
    ) {
        Box(Modifier.padding(20.dp)) { content() }
    }
}

@Composable
fun Badge(label: String, color: Color = MaterialTheme.colorScheme.tertiary) {
    Surface(
        color = color,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = AscendyColors.Ink
        )
    }
}

@Composable
fun Dot(color: Color, sizeDp: Int = 8) {
    Canvas(modifier = Modifier.padding(0.dp)) {
        drawCircle(color, radius = sizeDp.toFloat())
    }
}
