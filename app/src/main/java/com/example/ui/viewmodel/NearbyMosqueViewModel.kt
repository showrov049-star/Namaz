package com.example.ui.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.location.LocationManager
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MosqueInfo
import com.example.data.MosqueRepository
import com.example.data.MosqueUiStatus
import com.example.util.LocationHelper
import com.example.util.PermissionHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class NearbyMosqueUiState(
    val userLat: Double? = null,
    val userLng: Double? = null,
    val radiusKm: Int = 3,
    val searchQuery: String = "",
    val status: MosqueUiStatus = MosqueUiStatus.Idle,
    val selectedMosque: MosqueInfo? = null,
    val showListMode: Boolean = false,
    val usedFallbackNotice: Boolean = false,
    val hasLocationPermission: Boolean = false
)

class NearbyMosqueViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MosqueRepository()
    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow(
        NearbyMosqueUiState(
            hasLocationPermission = PermissionHelper.hasLocationPermission(application)
        )
    )
    val uiState: StateFlow<NearbyMosqueUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var allFetchedMosques: List<MosqueInfo> = emptyList()

    fun startDiscovery() {
        val ctx = getApplication<Application>()
        val hasPerm = PermissionHelper.hasLocationPermission(ctx)
        _uiState.update { it.copy(hasLocationPermission = hasPerm) }

        if (!hasPerm) {
            _uiState.update { it.copy(status = MosqueUiStatus.PermissionRequired) }
            return
        }

        if (!isGpsEnabled()) {
            _uiState.update { it.copy(status = MosqueUiStatus.GpsDisabled) }
            return
        }

        fetchGpsAndSearch()
    }

    fun onPermissionResult(permissions: Map<String, Boolean>) {
        val ctx = getApplication<Application>()
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            PermissionHelper.hasLocationPermission(ctx)

        _uiState.update { it.copy(hasLocationPermission = granted) }
        if (granted) {
            startDiscovery()
        } else {
            _uiState.update { it.copy(status = MosqueUiStatus.PermissionRequired) }
        }
    }

    fun selectRadiusKm(radiusKm: Int) {
        if (_uiState.value.radiusKm == radiusKm) return
        _uiState.update { it.copy(radiusKm = radiusKm, selectedMosque = null) }
        val lat = _uiState.value.userLat
        val lng = _uiState.value.userLng
        if (lat != null && lng != null) {
            performMosqueSearch(lat, lng, radiusKm, _uiState.value.searchQuery)
        } else {
            startDiscovery()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, selectedMosque = null) }
        val lat = _uiState.value.userLat ?: return
        val lng = _uiState.value.userLng ?: return

        // Instant local filter if we already have results for empty->text transitions
        if (allFetchedMosques.isNotEmpty() && query.isNotBlank()) {
            val filtered = allFetchedMosques.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true)
            }
            if (filtered.isNotEmpty()) {
                _uiState.update { it.copy(status = MosqueUiStatus.Success(filtered)) }
            }
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(450L)
            performMosqueSearch(lat, lng, _uiState.value.radiusKm, query)
        }
    }

    fun selectMosque(mosque: MosqueInfo?) {
        _uiState.update { it.copy(selectedMosque = mosque) }
    }

    fun toggleListMode(showList: Boolean) {
        _uiState.update { it.copy(showListMode = showList) }
    }

    fun refresh() {
        startDiscovery()
    }

    private fun isGpsEnabled(): Boolean {
        val lm = getApplication<Application>().getSystemService(LocationManager::class.java)
            ?: return false
        return LocationManagerCompat.isLocationEnabled(lm)
    }

    private fun fetchGpsAndSearch() {
        val ctx = getApplication<Application>()
        viewModelScope.launch {
            _uiState.update { it.copy(status = MosqueUiStatus.LoadingLocation) }

            val useFine = PermissionHelper.hasFineLocationPermission(ctx)
            val loc = fetchLiveLocation(useFine) ?: LocationHelper.fetchPlatformLastLocation(ctx)

            if (loc == null) {
                if (!isGpsEnabled()) {
                    _uiState.update { it.copy(status = MosqueUiStatus.GpsDisabled) }
                } else {
                    _uiState.update {
                        it.copy(
                            status = MosqueUiStatus.Error(
                                "Unable to acquire your GPS position. Please try again."
                            )
                        )
                    }
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    userLat = loc.latitude,
                    userLng = loc.longitude
                )
            }
            performMosqueSearch(
                latitude = loc.latitude,
                longitude = loc.longitude,
                radiusKm = _uiState.value.radiusKm,
                query = _uiState.value.searchQuery
            )
        }
    }

    private fun performMosqueSearch(
        latitude: Double,
        longitude: Double,
        radiusKm: Int,
        query: String
    ) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(status = MosqueUiStatus.LoadingMosques) }
            val result = repository.findNearbyMosques(
                latitude = latitude,
                longitude = longitude,
                radiusKm = radiusKm,
                query = query
            )
            result.fold(
                onSuccess = { data ->
                    if (query.isBlank()) {
                        allFetchedMosques = data.mosques
                    }
                    val status = if (data.mosques.isEmpty()) {
                        MosqueUiStatus.Empty(radiusKm = radiusKm, query = query)
                    } else {
                        MosqueUiStatus.Success(data.mosques)
                    }
                    _uiState.update {
                        it.copy(
                            status = status,
                            usedFallbackNotice = data.usedApiKeyFallback
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            status = MosqueUiStatus.Error(
                                err.message ?: "Failed to load nearby mosques."
                            )
                        )
                    }
                }
            )
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchLiveLocation(useFineAccuracy: Boolean): Location? =
        suspendCancellableCoroutine { cont ->
            val priority = if (useFineAccuracy) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }

            fusedClient.getCurrentLocation(priority, cts.token)
                .addOnSuccessListener { current ->
                    if (current != null) {
                        if (cont.isActive) cont.resume(current)
                    } else {
                        fusedClient.lastLocation
                            .addOnSuccessListener { last ->
                                if (cont.isActive) cont.resume(last)
                            }
                            .addOnFailureListener {
                                if (cont.isActive) cont.resume(null)
                            }
                    }
                }
                .addOnFailureListener {
                    fusedClient.lastLocation
                        .addOnSuccessListener { last ->
                            if (cont.isActive) cont.resume(last)
                        }
                        .addOnFailureListener {
                            if (cont.isActive) cont.resume(null)
                        }
                }
        }
}

fun formatDistance(meters: Float): String {
    return if (meters < 1000f) {
        "${meters.toInt()} m"
    } else {
        String.format(Locale.US, "%.1f km", meters / 1000f)
    }
}
