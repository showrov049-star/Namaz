package com.example.ui.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppLanguage
import com.example.data.CallEventInfo
import com.example.data.DailyPrayerTimes
import com.example.data.NamazRepository
import com.example.data.NamazSettingsState
import com.example.data.PrayerName
import com.example.data.PrayerNotificationConfig
import com.example.data.TasbihState
import com.example.data.ZikrOption
import com.example.receiver.PrayerNotificationReceiver
import com.example.service.NamazForegroundService
import com.example.service.NamazTileService
import com.example.service.PrayerAlarmScheduler
import com.example.service.TtsVoiceManager
import com.example.util.FeedbackManager
import com.example.util.LocationHelper
import com.example.util.PermissionHelper
import com.example.util.PrayerTimeCalculator
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

data class NamazUiState(
    val settings: NamazSettingsState = NamazSettingsState(),
    val remainingMillis: Long = 15 * 60_000L,
    val lastCallEvent: CallEventInfo = CallEventInfo(),
    val dailyPrayerTimes: DailyPrayerTimes = DailyPrayerTimes(),
    val nextPrayerCountdownMillis: Long = 0L,
    val prayerNotifications: PrayerNotificationConfig = PrayerNotificationConfig(),
    val tasbih: TasbihState = TasbihState(),
    val prayerTrackerHistory: Map<String, Set<PrayerName>> = emptyMap(),
    val duaBookmarks: Set<String> = emptySet(),
    val hasLocationPermission: Boolean = false,
    val hasFineLocationPermission: Boolean = false,
    val isDetectingLocation: Boolean = false,
    val locationError: Boolean = false,
    val shouldRequestLocationPermission: Boolean = false
)

class NamazViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NamazRepository(application)
    private val ttsManager = TtsVoiceManager(application)
    val feedbackManager = FeedbackManager(application)
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _uiState = MutableStateFlow(
        NamazUiState(
            hasLocationPermission = PermissionHelper.hasLocationPermission(application),
            hasFineLocationPermission = PermissionHelper.hasFineLocationPermission(application)
        )
    )
    val uiState: StateFlow<NamazUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settingsFlow.collect { settings ->
                val now = System.currentTimeMillis()
                val rem = if (settings.isNamazModeActive && settings.endTimeMillis > 0L) {
                    (settings.endTimeMillis - now).coerceAtLeast(0L)
                } else {
                    settings.durationMinutes * 60_000L
                }
                val prayerTimes = if (settings.hasSavedLocation) {
                    PrayerTimeCalculator.calculateForDate(
                        latitude = settings.latitude,
                        longitude = settings.longitude,
                        locationLabel = settings.locationName,
                        method = settings.calculationMethod,
                        asrSchool = settings.asrSchool
                    )
                } else {
                    DailyPrayerTimes()
                }
                val nextCountdown = if (prayerTimes.nextPrayerEpochMillis > now) {
                    prayerTimes.nextPrayerEpochMillis - now
                } else 0L

                feedbackManager.updatePreferences(
                    soundsEnabled = settings.touchSoundsEnabled,
                    hapticsEnabled = settings.hapticFeedbackEnabled,
                    volume = settings.soundVolume,
                    intensity = settings.hapticIntensity
                )
                _uiState.update {
                    it.copy(
                        settings = settings,
                        remainingMillis = rem,
                        dailyPrayerTimes = prayerTimes,
                        nextPrayerCountdownMillis = nextCountdown
                    )
                }
                PrayerAlarmScheduler.rescheduleAll(
                    getApplication(),
                    settings,
                    _uiState.value.prayerNotifications
                )
            }
        }
        viewModelScope.launch {
            repository.prayerNotificationsFlow.collect { config ->
                _uiState.update { it.copy(prayerNotifications = config) }
                PrayerAlarmScheduler.rescheduleAll(
                    getApplication(),
                    _uiState.value.settings,
                    config
                )
            }
        }
        viewModelScope.launch {
            repository.tasbihFlow.collect { tasbih ->
                _uiState.update { it.copy(tasbih = tasbih) }
            }
        }
        viewModelScope.launch {
            repository.prayerTrackerFlow.collect { history ->
                _uiState.update { it.copy(prayerTrackerHistory = history) }
            }
        }
        viewModelScope.launch {
            repository.duaBookmarksFlow.collect { bookmarks ->
                _uiState.update { it.copy(duaBookmarks = bookmarks) }
            }
        }
        viewModelScope.launch {
            NamazRepository.lastCallEvent.collect { call ->
                _uiState.update { it.copy(lastCallEvent = call) }
            }
        }
        viewModelScope.launch {
            var lastMinute = -1
            while (true) {
                val now = System.currentTimeMillis()
                val current = _uiState.value
                val s = current.settings
                if (s.isNamazModeActive && s.endTimeMillis > 0L) {
                    val rem = (s.endTimeMillis - now).coerceAtLeast(0L)
                    _uiState.update { it.copy(remainingMillis = rem) }
                }
                val currentMinute = java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE)
                val targetMillis = current.dailyPrayerTimes.nextPrayerEpochMillis
                if (s.hasSavedLocation && (currentMinute != lastMinute || (targetMillis in 1..now))) {
                    lastMinute = currentMinute
                    val refreshed = PrayerTimeCalculator.calculateForDate(
                        latitude = s.latitude,
                        longitude = s.longitude,
                        locationLabel = s.locationName,
                        method = s.calculationMethod,
                        asrSchool = s.asrSchool
                    )
                    val countdown = (refreshed.nextPrayerEpochMillis - now).coerceAtLeast(0L)
                    _uiState.update {
                        it.copy(
                            dailyPrayerTimes = refreshed,
                            nextPrayerCountdownMillis = countdown
                        )
                    }
                } else if (targetMillis > 0L) {
                    val countdown = (targetMillis - now).coerceAtLeast(0L)
                    _uiState.update { it.copy(nextPrayerCountdownMillis = countdown) }
                }
                delay(1000L)
            }
        }
        if (PermissionHelper.hasLocationPermission(application)) {
            refreshLocation()
        }
    }

    fun requestFineLocationPermission() {
        val ctx = getApplication<Application>()
        if (PermissionHelper.hasFineLocationPermission(ctx)) {
            refreshLocation()
        } else {
            _uiState.update { it.copy(shouldRequestLocationPermission = true) }
        }
    }

    fun consumeLocationPermissionRequest() {
        _uiState.update { it.copy(shouldRequestLocationPermission = false) }
    }

    fun onLocationPermissionsResult(permissions: Map<String, Boolean>) {
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val ctx = getApplication<Application>()
        val anyGranted = fineGranted || coarseGranted || PermissionHelper.hasLocationPermission(ctx)
        val hasFine = fineGranted || PermissionHelper.hasFineLocationPermission(ctx)

        _uiState.update {
            it.copy(
                hasLocationPermission = anyGranted,
                hasFineLocationPermission = hasFine,
                locationError = false,
                shouldRequestLocationPermission = false
            )
        }
        if (anyGranted) {
            refreshLocation()
        }
    }

    fun refreshLocation() {
        val ctx = getApplication<Application>()
        val permitted = PermissionHelper.hasLocationPermission(ctx)
        val finePermitted = PermissionHelper.hasFineLocationPermission(ctx)
        _uiState.update {
            it.copy(
                hasLocationPermission = permitted,
                hasFineLocationPermission = finePermitted
            )
        }
        if (!permitted) {
            requestFineLocationPermission()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDetectingLocation = true, locationError = false) }
            val location = fetchCurrentCoordinates(useFineAccuracy = finePermitted)
                ?: LocationHelper.fetchPlatformLastLocation(ctx)

            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                val cityName = LocationHelper.resolveLocationName(ctx, latitude, longitude)
                repository.saveLocation(
                    latitude = latitude,
                    longitude = longitude,
                    locationName = cityName
                )
                _uiState.update { it.copy(isDetectingLocation = false, locationError = false) }
            } else {
                _uiState.update { it.copy(isDetectingLocation = false, locationError = true) }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun fetchCurrentCoordinates(useFineAccuracy: Boolean): Location? =
        suspendCancellableCoroutine { cont ->
            val priority = if (useFineAccuracy) {
                Priority.PRIORITY_HIGH_ACCURACY
            } else {
                Priority.PRIORITY_BALANCED_POWER_ACCURACY
            }
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }

            fusedLocationClient.getCurrentLocation(priority, cts.token)
                .addOnSuccessListener { currentLocation ->
                    if (currentLocation != null) {
                        if (cont.isActive) cont.resume(currentLocation)
                    } else {
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { last ->
                                if (cont.isActive) cont.resume(last)
                            }
                            .addOnFailureListener {
                                if (cont.isActive) cont.resume(null)
                            }
                    }
                }
                .addOnFailureListener {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { last ->
                            if (cont.isActive) cont.resume(last)
                        }
                        .addOnFailureListener {
                            if (cont.isActive) cont.resume(null)
                        }
                }
        }

    fun toggleNamazMode() {
        val ctx = getApplication<Application>()
        val current = _uiState.value.settings
        viewModelScope.launch {
            if (current.isNamazModeActive) {
                repository.setActive(false, 0L)
                NamazForegroundService.stop(ctx)
            } else {
                val endTime = if (current.autoTurnOff) {
                    System.currentTimeMillis() + current.durationMinutes * 60_000L
                } else 0L
                repository.setActive(true, endTime)
                NamazForegroundService.start(ctx)
            }
            NamazTileService.requestTileRefresh(ctx)
        }
    }

    fun toggleAutoCall(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAutoCall(enabled)
        }
    }

    fun selectDurationMinutes(minutes: Int) {
        val ctx = getApplication<Application>()
        val current = _uiState.value.settings
        viewModelScope.launch {
            repository.setDurationMinutes(minutes)
            if (current.isNamazModeActive && current.autoTurnOff) {
                val newEnd = System.currentTimeMillis() + minutes * 60_000L
                repository.setActive(true, newEnd)
                NamazForegroundService.start(ctx)
            }
        }
    }

    fun toggleAppLanguage() {
        val current = _uiState.value.settings.appLanguage
        val next = if (current == AppLanguage.ENGLISH) AppLanguage.BENGALI else AppLanguage.ENGLISH
        viewModelScope.launch {
            repository.setAppLanguage(next)
        }
    }

    fun saveSettings(updated: NamazSettingsState) {
        val ctx = getApplication<Application>()
        viewModelScope.launch {
            repository.saveSettings(updated)
            if (updated.isNamazModeActive) {
                val newEnd = if (updated.autoTurnOff) {
                    System.currentTimeMillis() + updated.durationMinutes * 60_000L
                } else 0L
                repository.setActive(true, newEnd)
                NamazForegroundService.start(ctx)
            }
        }
    }

    fun restoreTouchSoundDefaults() {
        val defaults = NamazSettingsState()
        viewModelScope.launch {
            repository.updateTouchAndSound(
                touchSoundsEnabled = defaults.touchSoundsEnabled,
                hapticFeedbackEnabled = defaults.hapticFeedbackEnabled,
                soundVolume = defaults.soundVolume,
                hapticIntensity = defaults.hapticIntensity
            )
        }
    }

    fun updatePrayerNotifications(config: PrayerNotificationConfig) {
        viewModelScope.launch {
            repository.savePrayerNotifications(config)
            PrayerAlarmScheduler.rescheduleAll(
                getApplication(),
                _uiState.value.settings,
                config
            )
        }
    }

    fun sendTestPrayerNotification() {
        val s = _uiState.value.settings
        val notif = _uiState.value.prayerNotifications
        PrayerNotificationReceiver.postTestNotification(
            context = getApplication(),
            language = s.appLanguage,
            playSound = notif.playSound
        )
    }

    fun incrementTasbih(): Boolean {
        val current = _uiState.value.tasbih
        val nextCount = current.currentCount + 1
        val completedRound = nextCount >= current.roundTarget
        val nextDaily = current.dailyCount + 1
        val nextLifetime = current.lifetimeCount + 1L
        val updated = current.copy(
            currentCount = if (completedRound) 0 else nextCount,
            dailyCount = nextDaily,
            lifetimeCount = nextLifetime
        )
        _uiState.update { it.copy(tasbih = updated) }
        viewModelScope.launch {
            repository.saveTasbihState(updated)
        }
        return completedRound || nextDaily == current.dailyGoal
    }

    fun resetTasbihRound() {
        val updated = _uiState.value.tasbih.copy(currentCount = 0)
        _uiState.update { it.copy(tasbih = updated) }
        viewModelScope.launch { repository.saveTasbihState(updated) }
    }

    fun selectTasbihZikr(zikr: ZikrOption) {
        val updated = _uiState.value.tasbih.copy(
            selectedZikrId = zikr.id,
            roundTarget = zikr.defaultTarget,
            currentCount = 0
        )
        _uiState.update { it.copy(tasbih = updated) }
        viewModelScope.launch { repository.saveTasbihState(updated) }
    }

    fun setTasbihDailyGoal(goal: Int) {
        val clamped = goal.coerceIn(10, 5000)
        val updated = _uiState.value.tasbih.copy(dailyGoal = clamped)
        _uiState.update { it.copy(tasbih = updated) }
        viewModelScope.launch { repository.saveTasbihState(updated) }
    }

    fun setTasbihRoundTarget(target: Int) {
        val clamped = target.coerceIn(7, 500)
        val updated = _uiState.value.tasbih.copy(roundTarget = clamped, currentCount = 0)
        _uiState.update { it.copy(tasbih = updated) }
        viewModelScope.launch { repository.saveTasbihState(updated) }
    }

    fun togglePrayerTracked(dateKey: String, prayer: PrayerName) {
        viewModelScope.launch {
            repository.togglePrayerTracked(dateKey, prayer)
        }
    }

    fun toggleDuaBookmark(duaId: String) {
        viewModelScope.launch {
            repository.toggleDuaBookmark(duaId)
        }
    }

    fun triggerVoicePreview() {
        val settings = _uiState.value.settings
        val msg = settings.resolveSpokenMessage()
        NamazRepository.publishCallEvent(
            CallEventInfo(
                callerNumber = "Caller Preview",
                statusText = "Playing Prayer Announcement...",
                spokenMessage = msg,
                isActive = true
            )
        )
        ttsManager.speak(msg, settings.voiceLanguage) {}
    }

    fun stopVoiceAndDismissCall() {
        ttsManager.stop()
        NamazRepository.clearActiveCallEvent()
    }

    override fun onCleared() {
        ttsManager.shutdown()
        feedbackManager.release()
        super.onCleared()
    }
}

fun formatDurationHms(millis: Long): String {
    val totalSec = (millis / 1000L).coerceAtLeast(0L)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
}
