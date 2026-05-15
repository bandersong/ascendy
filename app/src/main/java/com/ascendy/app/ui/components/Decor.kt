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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ascendy.app.ui.theme.Palette
import com.ascendy.app.ui.theme.ThemeVariant
import com.ascendy.app.ui.theme.palette
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Mascot(locked: Boolean, modifier: Modifier = Modifier.fillMaxWidth()) {
    val p = palette
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
            drawMascot(p, locked, cx, cy, radius, withChains = p.variant == ThemeVariant.Tough)
        }
    }
}

/** A tiny static mascot for inline use (no animation). */
@Composable
fun MiniMascot(locked: Boolean, modifier: Modifier = Modifier) {
    val p = palette
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension * 0.38f
        drawMascot(p, locked, cx, cy, radius, withChains = false)
    }
}

private fun DrawScope.drawMascot(p: Palette, locked: Boolean, cx: Float, cy: Float, r: Float, withChains: Boolean) {
    when (p.variant) {
        ThemeVariant.Kawaii -> if (locked) drawKawaiiMoon(p, cx, cy, r) else drawKawaiiStar(p, cx, cy, r)
        ThemeVariant.Tough -> {
            if (locked) drawAngryMoon(p, cx, cy, r) else drawAngryStar(p, cx, cy, r)
            if (withChains) drawChains(p, cx, cy, r)
        }
    }
}

// ───── Kawaii drawings ─────

private fun DrawScope.drawKawaiiMoon(p: Palette, cx: Float, cy: Float, r: Float) {
    val body = p.Lilac
    val face = p.Ink
    val cheek = p.Petal

    val outer = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(cx - r, cy - r), Size(r * 2, r * 2))) }
    val cut = Path().apply { addOval(androidx.compose.ui.geometry.Rect(Offset(cx - r * 0.4f, cy - r), Size(r * 2, r * 2))) }
    val crescent = Path().apply { op(outer, cut, PathOperation.Difference) }

    drawPath(crescent, color = body)
    drawArc(
        color = face,
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.55f, cy - r * 0.2f),
        size = Size(r * 0.45f, r * 0.30f),
        style = Stroke(width = r * 0.08f)
    )
    drawCircle(color = cheek, radius = r * 0.12f, center = Offset(cx - r * 0.65f, cy + r * 0.15f))
}

private fun DrawScope.drawKawaiiStar(p: Palette, cx: Float, cy: Float, r: Float) {
    val body = p.Petal
    val face = p.Ink
    val cheek = p.Lilac
    val shine = p.Cream

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

    val eyeR = r * 0.12f
    val eyeY = cy - r * 0.05f
    drawCircle(color = face, radius = eyeR, center = Offset(cx - r * 0.24f, eyeY))
    drawCircle(color = face, radius = eyeR, center = Offset(cx + r * 0.24f, eyeY))
    drawCircle(color = shine, radius = eyeR * 0.35f, center = Offset(cx - r * 0.21f, eyeY - eyeR * 0.35f))
    drawCircle(color = shine, radius = eyeR * 0.35f, center = Offset(cx + r * 0.27f, eyeY - eyeR * 0.35f))

    drawArc(
        color = face,
        startAngle = 25f,
        sweepAngle = 130f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.16f, cy + r * 0.10f),
        size = Size(r * 0.32f, r * 0.20f),
        style = Stroke(width = r * 0.07f)
    )
    drawCircle(color = cheek, radius = r * 0.10f, center = Offset(cx - r * 0.46f, cy + r * 0.18f))
    drawCircle(color = cheek, radius = r * 0.10f, center = Offset(cx + r * 0.46f, cy + r * 0.18f))
}

// ───── Tough drawings ─────

private fun DrawScope.drawAngryStar(p: Palette, cx: Float, cy: Float, r: Float) {
    val body = p.Petal              // black/bone primary
    val face = p.Cream              // bg color = contrast color for tough body
    val accent = p.Lilac

    // 5-point spike star — sharper than the 8-point petal version
    val points = 5
    val outer = r * 1.10f
    val inner = r * 0.45f            // deeper notches = pointier
    val path = Path()
    for (i in 0 until points * 2) {
        val angle = (Math.PI * i / points).toFloat() - (Math.PI / 2).toFloat()
        val rad = if (i % 2 == 0) outer else inner
        val x = cx + rad * cos(angle)
        val y = cy + rad * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = body)
    // outline stroke for grit
    drawPath(path, color = p.Ink, style = Stroke(width = r * 0.04f))

    // angled eyebrows (angry) — slashes
    val browW = r * 0.06f
    drawLine(
        color = face,
        start = Offset(cx - r * 0.40f, cy - r * 0.18f),
        end = Offset(cx - r * 0.10f, cy - r * 0.05f),
        strokeWidth = browW
    )
    drawLine(
        color = face,
        start = Offset(cx + r * 0.40f, cy - r * 0.18f),
        end = Offset(cx + r * 0.10f, cy - r * 0.05f),
        strokeWidth = browW
    )

    // narrowed eyes (slits)
    val eyeY = cy + r * 0.05f
    drawLine(face, Offset(cx - r * 0.30f, eyeY), Offset(cx - r * 0.10f, eyeY), strokeWidth = r * 0.08f)
    drawLine(face, Offset(cx + r * 0.10f, eyeY), Offset(cx + r * 0.30f, eyeY), strokeWidth = r * 0.08f)

    // scowl (downward arc)
    drawArc(
        color = face,
        startAngle = 200f,
        sweepAngle = 140f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.20f, cy + r * 0.20f),
        size = Size(r * 0.40f, r * 0.20f),
        style = Stroke(width = r * 0.07f)
    )

    // tiny scar slash on cheek
    drawLine(
        color = accent,
        start = Offset(cx - r * 0.55f, cy - r * 0.30f),
        end = Offset(cx - r * 0.40f, cy - r * 0.10f),
        strokeWidth = r * 0.05f
    )
}

private fun DrawScope.drawAngryMoon(p: Palette, cx: Float, cy: Float, r: Float) {
    // Skull-esque: filled circle with sunken eye sockets + clenched jaw
    val body = if (p.isDark) p.Petal else p.Mist     // bone tone in both modes
    val face = p.Ink
    val accent = p.Lilac

    drawCircle(color = body, radius = r * 0.95f, center = Offset(cx, cy))
    drawCircle(color = face, radius = r * 0.95f, center = Offset(cx, cy), style = Stroke(width = r * 0.04f))

    // black sunken eye sockets
    drawCircle(color = face, radius = r * 0.18f, center = Offset(cx - r * 0.30f, cy - r * 0.12f))
    drawCircle(color = face, radius = r * 0.18f, center = Offset(cx + r * 0.30f, cy - r * 0.12f))

    // angry brow ridges
    drawLine(
        color = face,
        start = Offset(cx - r * 0.50f, cy - r * 0.40f),
        end = Offset(cx - r * 0.10f, cy - r * 0.22f),
        strokeWidth = r * 0.08f
    )
    drawLine(
        color = face,
        start = Offset(cx + r * 0.50f, cy - r * 0.40f),
        end = Offset(cx + r * 0.10f, cy - r * 0.22f),
        strokeWidth = r * 0.08f
    )

    // clenched teeth — vertical strokes
    val toothY1 = cy + r * 0.20f
    val toothY2 = cy + r * 0.50f
    drawLine(face, Offset(cx - r * 0.35f, toothY1), Offset(cx + r * 0.35f, toothY1), strokeWidth = r * 0.04f)
    drawLine(face, Offset(cx - r * 0.35f, toothY2), Offset(cx + r * 0.35f, toothY2), strokeWidth = r * 0.04f)
    for (i in -2..2) {
        val x = cx + i * r * 0.16f
        drawLine(face, Offset(x, toothY1), Offset(x, toothY2), strokeWidth = r * 0.04f)
    }

    // tiny accent (red eye glint)
    drawCircle(color = accent, radius = r * 0.04f, center = Offset(cx - r * 0.26f, cy - r * 0.15f))
    drawCircle(color = accent, radius = r * 0.04f, center = Offset(cx + r * 0.34f, cy - r * 0.15f))
}

/** Draw a pair of iron chains flanking the mascot. */
private fun DrawScope.drawChains(p: Palette, cx: Float, cy: Float, r: Float) {
    val chain = p.Smoke
    val stroke = r * 0.09f
    val linkW = r * 0.30f
    val linkH = r * 0.16f

    fun link(x: Float, y: Float) {
        drawArc(
            color = chain,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(x - linkW / 2, y - linkH / 2),
            size = Size(linkW, linkH),
            style = Stroke(width = stroke)
        )
    }

    // left chain — 4 links going up-left from mascot edge
    var lx = cx - r * 1.15f
    var ly = cy + r * 0.10f
    for (i in 0..3) {
        link(lx, ly)
        lx -= linkW * 0.55f
        ly -= linkH * 1.10f
    }

    // right chain — 4 links going up-right
    var rx = cx + r * 1.15f
    var ry = cy + r * 0.10f
    for (i in 0..3) {
        link(rx, ry)
        rx += linkW * 0.55f
        ry -= linkH * 1.10f
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
    val onColor = if (palette.variant == ThemeVariant.Tough && color == palette.Petal) palette.Cream else palette.Ink
    Surface(
        color = color,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = onColor
        )
    }
}

@Composable
fun Dot(color: Color, sizeDp: Int = 8) {
    Canvas(modifier = Modifier.padding(0.dp)) {
        drawCircle(color, radius = sizeDp.toFloat())
    }
}
