package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class DetectedLocation(
    val latitude: Double,
    val longitude: Double,
    val displayName: String
)

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun detectCurrentLocation(context: Context): DetectedLocation? {
        if (!PermissionHelper.hasLocationPermission(context)) return null

        val fusedLocation = fetchFusedLocation(context)
            ?: fetchPlatformLastLocation(context)
            ?: return null

        val cityName = resolveLocationName(
            context = context,
            latitude = fusedLocation.latitude,
            longitude = fusedLocation.longitude
        )

        return DetectedLocation(
            latitude = fusedLocation.latitude,
            longitude = fusedLocation.longitude,
            displayName = cityName
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchFusedLocation(context: Context): Location? =
        suspendCancellableCoroutine { cont ->
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }

            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { current ->
                    if (current != null) {
                        if (cont.isActive) cont.resume(current)
                    } else {
                        client.lastLocation
                            .addOnSuccessListener { last ->
                                if (cont.isActive) cont.resume(last)
                            }
                            .addOnFailureListener {
                                if (cont.isActive) cont.resume(null)
                            }
                    }
                }
                .addOnFailureListener {
                    client.lastLocation
                        .addOnSuccessListener { last ->
                            if (cont.isActive) cont.resume(last)
                        }
                        .addOnFailureListener {
                            if (cont.isActive) cont.resume(null)
                        }
                }
        }

    @SuppressLint("MissingPermission")
    fun fetchPlatformLastLocation(context: Context): Location? {
        val lm = context.getSystemService(LocationManager::class.java) ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        var best: Location? = null
        for (provider in providers) {
            val loc = runCatching { lm.getLastKnownLocation(provider) }.getOrNull()
            if (loc != null && (best == null || loc.time > best.time)) {
                best = loc
            }
        }
        return best
    }

    suspend fun resolveLocationName(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String = withContext(Dispatchers.IO) {
        val coordsFallback = String.format(Locale.US, "%.2f°, %.2f°", latitude, longitude)
        if (!Geocoder.isPresent()) return@withContext coordsFallback

        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                runCatching {
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val label = formatAddress(addresses.firstOrNull()) ?: coordsFallback
                        if (cont.isActive) cont.resume(label)
                    }
                }.onFailure {
                    if (cont.isActive) cont.resume(coordsFallback)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val addresses = runCatching { geocoder.getFromLocation(latitude, longitude, 1) }.getOrNull()
            formatAddress(addresses?.firstOrNull()) ?: coordsFallback
        }
    }

    private fun formatAddress(address: Address?): String? {
        if (address == null) return null
        val city = address.locality ?: address.subAdminArea ?: address.adminArea
        val country = address.countryCode
        return when {
            !city.isNullOrBlank() && !country.isNullOrBlank() -> "$city, $country"
            !city.isNullOrBlank() -> city
            !address.countryName.isNullOrBlank() -> address.countryName
            else -> null
        }
    }
}
