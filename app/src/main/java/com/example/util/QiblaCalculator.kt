package com.example.util

import android.hardware.GeomagneticField
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class QiblaDetails(
    val bearingDegrees: Float,
    val distanceKm: Double,
    val cardinalDirection: String
)

object QiblaCalculator {
    // Coordinates of the Sacred Kaaba in Makkah, Saudi Arabia
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LNG = 39.826206

    fun calculate(latitude: Double, longitude: Double): QiblaDetails {
        val lat1 = Math.toRadians(latitude)
        val lon1 = Math.toRadians(longitude)
        val lat2 = Math.toRadians(KAABA_LAT)
        val lon2 = Math.toRadians(KAABA_LNG)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val bearing = ((Math.toDegrees(atan2(y, x)) + 360.0) % 360.0).toFloat()

        val earthRadiusKm = 6371.0
        val dLat = lat2 - lat1
        val a = sin(dLat / 2.0).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2.0).pow(2)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        val distance = earthRadiusKm * c

        return QiblaDetails(
            bearingDegrees = bearing,
            distanceKm = distance,
            cardinalDirection = cardinalLabel(bearing)
        )
    }

    fun getDeclination(latitude: Double, longitude: Double, altitudeMeters: Double = 0.0): Float {
        return runCatching {
            GeomagneticField(
                latitude.toFloat(),
                longitude.toFloat(),
                altitudeMeters.toFloat(),
                System.currentTimeMillis()
            ).declination
        }.getOrDefault(0f)
    }

    fun normalizeAngle(angle: Float): Float {
        var res = angle % 360f
        if (res < 0f) res += 360f
        return res
    }

    fun shortestAngleDiff(current: Float, target: Float): Float {
        return ((target - current + 540f) % 360f) - 180f
    }

    private fun cardinalLabel(deg: Float): String {
        val dirs = listOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val idx = (((deg + 11.25f) % 360f) / 22.5f).toInt().coerceIn(0, dirs.lastIndex)
        return dirs[idx]
    }
}
