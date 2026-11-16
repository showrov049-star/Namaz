package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.BgDeepForest
import com.example.ui.theme.BgEmeraldNight
import com.example.ui.theme.BgNightTop
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldInactiveRing
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SilhouetteDark
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IslamicNightBackdrop(
    modifier: Modifier = Modifier,
    showMoon: Boolean = true,
    showMosqueArt: Boolean = true,
    showPrayingSilhouette: Boolean = false
) {
    val reduceMotion = rememberReduceMotionEnabled()
    val isForeground = rememberIsAppForeground()
    val animateAmbient = !reduceMotion && isForeground

    val ambientPhase = if (animateAmbient) {
        val transition = rememberInfiniteTransition(label = "ambientSky")
        val phase by transition.animateFloat(
            initialValue = -1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 5200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "moonDrift"
        )
        phase
    } else 0f

    Box(modifier = modifier.fillMaxSize().background(BgDeepForest)) {
        if (showMosqueArt) {
            Image(
                painter = painterResource(id = R.drawable.img_mosque_sanctuary),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val s = 1.03f + (ambientPhase * 0.012f)
                        scaleX = s
                        scaleY = s
                        translationY = ambientPhase * 6f
                    }
                    .alpha(if (showPrayingSilhouette) 0.42f else 0.24f)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BgNightTop.copy(alpha = 0.85f),
                        BgEmeraldNight.copy(alpha = 0.72f),
                        BgDeepForest.copy(alpha = 0.94f)
                    )
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GoldAccent.copy(alpha = 0.13f + (ambientPhase * 0.02f)),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.14f + ambientPhase * 6f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.5f, h * 0.14f + ambientPhase * 6f)
            )

            val starOffsets = listOf(
                0.12f to 0.08f, 0.25f to 0.14f, 0.78f to 0.09f,
                0.88f to 0.17f, 0.18f to 0.24f, 0.82f to 0.27f,
                0.38f to 0.06f, 0.64f to 0.05f
            )
            starOffsets.forEachIndexed { idx, (sx, sy) ->
                val twinkle = if (idx % 2 == 0) {
                    (0.55f + 0.22f * ambientPhase).coerceIn(0.2f, 0.9f)
                } else {
                    (0.45f - 0.16f * ambientPhase).coerceIn(0.2f, 0.85f)
                }
                drawCircle(
                    color = GoldAccent.copy(alpha = twinkle),
                    radius = if (idx % 2 == 0) 2.5f else 1.6f,
                    center = Offset(w * sx, h * sy)
                )
            }

            if (showMoon) {
                val moonCenter = Offset(w * 0.5f, h * 0.09f + (ambientPhase * 8f))
                val moonR = (w * 0.055f).coerceIn(18f, 34f)
                drawCircle(color = GoldAccent, radius = moonR, center = moonCenter)
                drawCircle(
                    color = BgNightTop,
                    radius = moonR * 0.85f,
                    center = Offset(moonCenter.x - moonR * 0.34f, moonCenter.y - moonR * 0.15f)
                )
            }

            if (showPrayingSilhouette) {
                val cx = w * 0.5f
                val cy = h * 0.44f
                drawArc(
                    brush = Brush.verticalGradient(
                        listOf(GoldAccent.copy(alpha = 0.85f), EmeraldGlow.copy(alpha = 0.12f))
                    ),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(cx - w * 0.34f, cy - w * 0.34f),
                    size = Size(w * 0.68f, w * 0.68f),
                    style = Stroke(width = 3.5f)
                )
                val prayPath = Path().apply {
                    moveTo(cx - 46f, cy + 70f)
                    quadraticTo(cx - 20f, cy + 22f, cx + 10f, cy + 28f)
                    quadraticTo(cx + 35f, cy + 36f, cx + 45f, cy + 70f)
                    close()
                }
                drawPath(prayPath, color = SilhouetteDark)
                drawCircle(
                    color = SilhouetteDark,
                    radius = 15f,
                    center = Offset(cx + 12f, cy + 12f)
                )
            }
        }
    }
}

@Composable
fun GlowingMosqueArchFrame(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val reduceMotion = rememberReduceMotionEnabled()
    val glowProgress by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = if (reduceMotion) tween(0) else tween(NamazMotion.LARGE_MS, easing = FastOutSlowInEasing),
        label = "archGlowProgress"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val color = if (isActive) EmeraldGlow else EmeraldInactiveRing
        val glowRadius = w * (0.50f + 0.18f * glowProgress)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = 0.10f + 0.24f * glowProgress),
                    Color.Transparent
                )
            ),
            radius = glowRadius,
            center = Offset(w / 2f, h * 0.56f)
        )

        val archPath = Path().apply {
            moveTo(w * 0.12f, h * 0.90f)
            lineTo(w * 0.12f, h * 0.48f)
            quadraticTo(w * 0.12f, h * 0.16f, w * 0.50f, h * 0.06f)
            quadraticTo(w * 0.88f, h * 0.16f, w * 0.88f, h * 0.48f)
            lineTo(w * 0.88f, h * 0.90f)
        }
        drawPath(
            path = archPath,
            color = color.copy(alpha = 0.46f + 0.44f * glowProgress),
            style = Stroke(width = 4f + 1.5f * glowProgress)
        )

        for (angleDeg in listOf(-140.0, -90.0, -40.0)) {
            val rad = angleDeg * PI / 180.0
            val px = (w / 2f) + (w * 0.43f * cos(rad)).toFloat()
            val py = (h * 0.44f) + (h * 0.36f * sin(rad)).toFloat()
            drawCircle(
                color = GoldAccent.copy(alpha = 0.45f + 0.42f * glowProgress),
                radius = 3.5f + 1.5f * glowProgress,
                center = Offset(px, py)
            )
        }
    }
}
