package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.IslamicNightBackdrop
import com.example.ui.theme.BgDeepForest
import com.example.ui.theme.BorderEmeraldGlow
import com.example.ui.theme.CardDarkEmerald
import com.example.ui.theme.CardElevated
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSoft
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.NamazUiState
import com.example.util.AppStrings
import com.example.util.FeedbackAction
import com.example.util.LocalFeedbackManager
import com.example.util.QiblaCalculator
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(
    state: NamazUiState,
    onRequestLocation: () -> Unit,
    onRefreshLocation: () -> Unit
) {
    val context = LocalContext.current
    val feedback = LocalFeedbackManager.current
    val s = AppStrings.get(state.settings.appLanguage)

    val hasCoords = state.settings.hasSavedLocation
    val qiblaDetails = remember(state.settings.latitude, state.settings.longitude) {
        QiblaCalculator.calculate(state.settings.latitude, state.settings.longitude)
    }
    val declination = remember(state.settings.latitude, state.settings.longitude, hasCoords) {
        if (hasCoords) QiblaCalculator.getDeclination(state.settings.latitude, state.settings.longitude) else 0f
    }

    var rawMagneticHeading by remember { mutableFloatStateOf(0f) }
    var continuousCompassRotation by remember { mutableFloatStateOf(0f) }
    var sensorAccuracy by remember { mutableIntStateOf(SensorManager.SENSOR_STATUS_ACCURACY_HIGH) }
    var hasCompassSensor by remember { mutableStateOf(true) }

    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (sensorManager == null || (rotationSensor == null && (accelSensor == null || magnetSensor == null))) {
            hasCompassSensor = false
            onDispose { }
        } else {
            hasCompassSensor = true
            val gravity = FloatArray(3)
            val geomagnetic = FloatArray(3)
            var hasGravity = false
            var hasMagnet = false
            val rotMatrix = FloatArray(9)
            val orientation = FloatArray(3)

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ROTATION_VECTOR -> {
                            SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                            SensorManager.getOrientation(rotMatrix, orientation)
                            val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            val azimuth = QiblaCalculator.normalizeAngle(deg)
                            val delta = QiblaCalculator.shortestAngleDiff(rawMagneticHeading, azimuth)
                            rawMagneticHeading = QiblaCalculator.normalizeAngle(rawMagneticHeading + delta * 0.22f)
                        }
                        Sensor.TYPE_ACCELEROMETER -> {
                            val alpha = 0.8f
                            for (i in 0..2) gravity[i] = alpha * gravity[i] + (1 - alpha) * event.values[i]
                            hasGravity = true
                        }
                        Sensor.TYPE_MAGNETIC_FIELD -> {
                            val alpha = 0.8f
                            for (i in 0..2) geomagnetic[i] = alpha * geomagnetic[i] + (1 - alpha) * event.values[i]
                            hasMagnet = true
                        }
                    }
                    if (rotationSensor == null && hasGravity && hasMagnet) {
                        if (SensorManager.getRotationMatrix(rotMatrix, null, gravity, geomagnetic)) {
                            SensorManager.getOrientation(rotMatrix, orientation)
                            val deg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                            val azimuth = QiblaCalculator.normalizeAngle(deg)
                            val delta = QiblaCalculator.shortestAngleDiff(rawMagneticHeading, azimuth)
                            rawMagneticHeading = QiblaCalculator.normalizeAngle(rawMagneticHeading + delta * 0.2f)
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    sensorAccuracy = accuracy
                }
            }

            if (rotationSensor != null) {
                sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            } else {
                sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
                sensorManager.registerListener(listener, magnetSensor, SensorManager.SENSOR_DELAY_UI)
            }

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }

    val trueHeading = QiblaCalculator.normalizeAngle(rawMagneticHeading + declination)
    val qiblaRelativeDiff = QiblaCalculator.shortestAngleDiff(trueHeading, qiblaDetails.bearingDegrees)
    val isAligned = hasCoords && hasCompassSensor && abs(qiblaRelativeDiff) <= 4f

    LaunchedEffect(trueHeading) {
        val targetDialAngle = -trueHeading
        val currentMod = QiblaCalculator.normalizeAngle(-continuousCompassRotation)
        val delta = QiblaCalculator.shortestAngleDiff(currentMod, trueHeading)
        continuousCompassRotation -= delta
    }

    var wasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(isAligned) {
        if (isAligned && !wasAligned) {
            feedback?.play(FeedbackAction.MARKER_POP)
        }
        wasAligned = isAligned
    }

    val animatedDialAngle by animateFloatAsState(
        targetValue = continuousCompassRotation,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "qiblaDial"
    )
    val ringAccent by animateColorAsState(
        targetValue = if (isAligned) EmeraldGlow else GoldAccent,
        label = "qiblaRing"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        IslamicNightBackdrop(showMoon = true, showMosqueArt = false)

        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Explore, contentDescription = null, tint = GoldAccent)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = s.qiblaCompassTitle,
                            color = TextPrimary,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = s.qiblaSub,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                if (state.hasLocationPermission) {
                    IconButton(
                        onClick = {
                            feedback?.play(FeedbackAction.HOME_CLICK)
                            onRefreshLocation()
                        },
                        modifier = Modifier.testTag("qibla_refresh_location")
                    ) {
                        if (state.isDetectingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = GoldAccent
                            )
                        } else {
                            Icon(Icons.Rounded.Refresh, contentDescription = s.refreshLocationDesc, tint = GoldAccent)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (!state.hasLocationPermission && !hasCoords) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = CardDarkEmerald.copy(alpha = 0.94f),
                    border = BorderStroke(1.dp, GoldSoft.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Rounded.MyLocation,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = s.permissionDeniedDesc,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 19.sp
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                feedback?.play(FeedbackAction.HOME_CLICK)
                                onRequestLocation()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("qibla_grant_location_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text(s.detectLocation, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                // Status Alignment Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isAligned) EmeraldPrimary.copy(alpha = 0.25f) else CardDarkEmerald,
                    border = BorderStroke(1.dp, if (isAligned) EmeraldGlow else GoldSoft.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mosque,
                            contentDescription = null,
                            tint = if (isAligned) EmeraldGlow else GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isAligned) s.facingQibla else s.rotateToQibla,
                            color = if (isAligned) EmeraldGlow else TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                // Live Animated Compass Dial
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .testTag("qibla_compass_dial"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val radius = size.minDimension / 2f - 10f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        // Outer metallic emerald-gold bezel
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(CardElevated, BgDeepForest),
                                center = center,
                                radius = radius
                            ),
                            radius = radius,
                            center = center
                        )
                        drawCircle(
                            color = ringAccent.copy(alpha = 0.7f),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 3.5f)
                        )

                        // Top phone reference indicator
                        drawLine(
                            color = if (isAligned) EmeraldGlow else GoldAccent,
                            start = Offset(center.x, center.y - radius - 8f),
                            end = Offset(center.x, center.y - radius + 14f),
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )

                        rotate(degrees = animatedDialAngle, pivot = center) {
                            // Degree ticks
                            for (deg in 0 until 360 step 10) {
                                val rad = Math.toRadians(deg.toDouble() - 90.0)
                                val isCardinal = deg % 90 == 0
                                val isMajor = deg % 30 == 0
                                val innerR = radius - when {
                                    isCardinal -> 24f
                                    isMajor -> 16f
                                    else -> 9f
                                }
                                val tickColor = when {
                                    deg == 0 -> GoldAccent
                                    isCardinal -> EmeraldGlow
                                    else -> TextSecondary.copy(alpha = 0.55f)
                                }
                                drawLine(
                                    color = tickColor,
                                    start = Offset(
                                        center.x + (innerR * cos(rad)).toFloat(),
                                        center.y + (innerR * sin(rad)).toFloat()
                                    ),
                                    end = Offset(
                                        center.x + ((radius - 3f) * cos(rad)).toFloat(),
                                        center.y + ((radius - 3f) * sin(rad)).toFloat()
                                    ),
                                    strokeWidth = if (isCardinal) 4f else 2f,
                                    cap = StrokeCap.Round
                                )
                            }

                            // Qibla Needle pointing at bearingDegrees on compass rose
                            rotate(degrees = qiblaDetails.bearingDegrees, pivot = center) {
                                val needlePath = Path().apply {
                                    moveTo(center.x, center.y - radius + 34f)
                                    lineTo(center.x - 16f, center.y)
                                    lineTo(center.x, center.y + 38f)
                                    lineTo(center.x + 16f, center.y)
                                    close()
                                }
                                drawPath(
                                    path = needlePath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(GoldAccent, EmeraldPrimary, CardDarkEmerald)
                                    )
                                )
                                drawCircle(
                                    color = GoldAccent,
                                    radius = 12f,
                                    center = Offset(center.x, center.y - radius + 40f)
                                )
                            }
                        }

                        drawCircle(
                            color = CardDarkEmerald,
                            radius = 14f,
                            center = center
                        )
                        drawCircle(
                            color = GoldAccent,
                            radius = 6f,
                            center = center
                        )
                    }

                    // Cardinal labels overlaid nicely
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(Modifier.height(110.dp))
                        Text(
                            text = "${qiblaDetails.bearingDegrees.toInt()}° ${qiblaDetails.cardinalDirection}",
                            color = GoldAccent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Info Cards: Bearing & Distance + Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = CardDarkEmerald.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, BorderEmeraldGlow)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(s.qiblaBearingLabel, color = TextSecondary, fontSize = 12.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%.1f° %s",
                                    qiblaDetails.bearingDegrees,
                                    qiblaDetails.cardinalDirection
                                ),
                                color = GoldAccent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = CardDarkEmerald.copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, BorderEmeraldGlow)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(s.distanceToMakkah, color = TextSecondary, fontSize = 12.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.US, "%,.0f km", qiblaDetails.distanceKm),
                                color = EmeraldGlow,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardDarkEmerald.copy(alpha = 0.92f),
                    border = BorderStroke(1.dp, BorderEmeraldGlow)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = EmeraldGlow)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.settings.locationName.ifBlank { "GPS Location" },
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%.4f°, %.4f°",
                                    state.settings.latitude,
                                    state.settings.longitude
                                ),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (!hasCompassSensor) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = s.compassUnavailable,
                        color = GoldSoft,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                } else if (sensorAccuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = s.calibrateCompassHint,
                        color = GoldSoft,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
