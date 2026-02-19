package com.smokesignal.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Siri-like animated aura rings that react to touch.
 * Drawn entirely on Canvas — zero bitmaps.
 */
@Composable
fun AuraVisualizer(isActive: Boolean, modifier: Modifier = Modifier) {

    val inf = rememberInfiniteTransition(label = "aura")

    val w1 by inf.animateFloat(0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart), label = "w1")
    val w2 by inf.animateFloat(0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(4500, easing = LinearEasing), RepeatMode.Restart), label = "w2")
    val w3 by inf.animateFloat(0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart), label = "w3")

    val scale by animateFloatAsState(
        if (isActive) 1.4f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "s"
    )
    val alpha by animateFloatAsState(if (isActive) 1f else 0.35f, tween(400), label = "a")

    val activeColor = Color(0xFF00D4FF)
    val idleColor = Color(0xFF6C5CE7)

    Canvas(modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val base = size.minDimension / 3.2f

        // aura rings
        for (i in 0..4) {
            val phase = i * (PI / 2.5f).toFloat()
            val d = sin(w1 + phase) * 18f + cos(w2 + phase) * 12f
            val r = (base * (1f + i * 0.28f) + d) * scale
            val ringAlpha = (alpha * (0.55f - i * 0.1f)).coerceAtLeast(0.02f)
            val color = if (isActive) activeColor else idleColor

            drawCircle(
                brush = Brush.radialGradient(
                    listOf(color.copy(ringAlpha), color.copy(ringAlpha * 0.4f), Color.Transparent),
                    center = Offset(cx, cy), radius = r
                ),
                radius = r, center = Offset(cx, cy)
            )
        }

        // particles when active
        if (isActive) {
            for (i in 0 until 14) {
                val angle = (2 * PI * i / 14) + w1 * 0.5
                val dist = base * (1.3f + sin(w3 + i).toFloat() * 0.25f) * scale
                val px = cx + cos(angle).toFloat() * dist
                val py = cy + sin(angle).toFloat() * dist
                val pr = 3f + sin(w2 + i * 2).toFloat() * 2f
                drawCircle(activeColor.copy(0.75f), pr, Offset(px, py))
            }
        }
    }
}
