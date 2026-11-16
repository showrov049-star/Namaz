package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "namaz_prefs")

class NamazRepository(private val context: Context) {

    private object Keys {
        val IS_ACTIVE = booleanPreferencesKey("is_active")
        val AUTO_CALL = booleanPreferencesKey("auto_call")
        val STRATEGY = stringPreferencesKey("call_strategy")
        val VOICE_LANG = stringPreferencesKey("voice_lang")
        val MSG_PRESET = stringPreferencesKey("msg_preset")
        val CUSTOM_BN = stringPreferencesKey("custom_bn")
        val CUSTOM_EN = stringPreferencesKey("custom_en")
        val DURATION_MIN = intPreferencesKey("duration_min")
        val AUTO_OFF = booleanPreferencesKey("auto_off")
        val APP_LANG = stringPreferencesKey("app_lang")
        val END_TIME = longPreferencesKey("end_time")
        val HAS_LOCATION = booleanPreferencesKey("has_location")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val LOCATION_NAME = stringPreferencesKey("location_name")
        val CALC_METHOD = stringPreferencesKey("calc_method")
        val ASR_SCHOOL = stringPreferencesKey("asr_school")
        val TOUCH_SOUNDS = booleanPreferencesKey("touch_sounds")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val SOUND_VOLUME = floatPreferencesKey("sound_volume")
        val HAPTIC_INTENSITY = stringPreferencesKey("haptic_intensity")

        // Prayer notifications
        val NOTIF_ENABLED = booleanPreferencesKey("prayer_notif_enabled")
        val NOTIF_FAJR = booleanPreferencesKey("prayer_notif_fajr")
        val NOTIF_DHUHR = booleanPreferencesKey("prayer_notif_dhuhr")
        val NOTIF_ASR = booleanPreferencesKey("prayer_notif_asr")
        val NOTIF_MAGHRIB = booleanPreferencesKey("prayer_notif_maghrib")
        val NOTIF_ISHA = booleanPreferencesKey("prayer_notif_isha")
        val NOTIF_MIN_BEFORE = intPreferencesKey("prayer_notif_min_before")
        val NOTIF_SOUND = booleanPreferencesKey("prayer_notif_sound")

        // Digital Tasbih
        val TASBIH_ZIKR_ID = stringPreferencesKey("tasbih_zikr_id")
        val TASBIH_CURRENT = intPreferencesKey("tasbih_current_count")
        val TASBIH_TARGET = intPreferencesKey("tasbih_round_target")
        val TASBIH_DAILY_GOAL = intPreferencesKey("tasbih_daily_goal")
        val TASBIH_DAILY_COUNT = intPreferencesKey("tasbih_daily_count")
        val TASBIH_LIFETIME = longPreferencesKey("tasbih_lifetime_count")
        val TASBIH_DATE = stringPreferencesKey("tasbih_date_key")

        // Prayer Tracker & Dua bookmarks
        val TRACKER_JSON = stringPreferencesKey("prayer_tracker_json")
        val DUA_BOOKMARKS = stringPreferencesKey("dua_bookmarks_csv")
    }

    val settingsFlow: Flow<NamazSettingsState> = context.dataStore.data.map { prefs ->
        NamazSettingsState(
            isNamazModeActive = prefs[Keys.IS_ACTIVE] ?: false,
            autoCallResponseEnabled = prefs[Keys.AUTO_CALL] ?: false,
            callStrategy = runCatching {
                CallHandlingStrategy.valueOf(
                    prefs[Keys.STRATEGY] ?: CallHandlingStrategy.SCREEN_AND_REJECT.name
                )
            }.getOrDefault(CallHandlingStrategy.SCREEN_AND_REJECT),
            voiceLanguage = runCatching {
                VoiceLanguage.valueOf(prefs[Keys.VOICE_LANG] ?: VoiceLanguage.BENGALI.name)
            }.getOrDefault(VoiceLanguage.BENGALI),
            messagePreset = runCatching {
                VoiceMessagePreset.valueOf(
                    prefs[Keys.MSG_PRESET] ?: VoiceMessagePreset.DEFAULT.name
                )
            }.getOrDefault(VoiceMessagePreset.DEFAULT),
            customMessageBn = prefs[Keys.CUSTOM_BN] ?: NamazSettingsState.DEFAULT_MESSAGE_BN,
            customMessageEn = prefs[Keys.CUSTOM_EN] ?: NamazSettingsState.DEFAULT_MESSAGE_EN,
            durationMinutes = prefs[Keys.DURATION_MIN] ?: 15,
            autoTurnOff = prefs[Keys.AUTO_OFF] ?: true,
            appLanguage = runCatching {
                AppLanguage.valueOf(prefs[Keys.APP_LANG] ?: AppLanguage.ENGLISH.name)
            }.getOrDefault(AppLanguage.ENGLISH),
            endTimeMillis = prefs[Keys.END_TIME] ?: 0L,
            hasSavedLocation = prefs[Keys.HAS_LOCATION] ?: false,
            latitude = prefs[Keys.LATITUDE] ?: 0.0,
            longitude = prefs[Keys.LONGITUDE] ?: 0.0,
            locationName = prefs[Keys.LOCATION_NAME] ?: "",
            calculationMethod = runCatching {
                PrayerCalculationMethod.valueOf(
                    prefs[Keys.CALC_METHOD] ?: PrayerCalculationMethod.KARACHI.name
                )
            }.getOrDefault(PrayerCalculationMethod.KARACHI),
            asrSchool = runCatching {
                AsrJuristicSchool.valueOf(
                    prefs[Keys.ASR_SCHOOL] ?: AsrJuristicSchool.HANAFI.name
                )
            }.getOrDefault(AsrJuristicSchool.HANAFI),
            touchSoundsEnabled = prefs[Keys.TOUCH_SOUNDS] ?: true,
            hapticFeedbackEnabled = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            soundVolume = (prefs[Keys.SOUND_VOLUME] ?: 0.75f).coerceIn(0f, 1f),
            hapticIntensity = runCatching {
                HapticIntensity.valueOf(
                    prefs[Keys.HAPTIC_INTENSITY] ?: HapticIntensity.MEDIUM.name
                )
            }.getOrDefault(HapticIntensity.MEDIUM)
        )
    }

    val prayerNotificationsFlow: Flow<PrayerNotificationConfig> = context.dataStore.data.map { prefs ->
        PrayerNotificationConfig(
            enabled = prefs[Keys.NOTIF_ENABLED] ?: false,
            fajrEnabled = prefs[Keys.NOTIF_FAJR] ?: true,
            dhuhrEnabled = prefs[Keys.NOTIF_DHUHR] ?: true,
            asrEnabled = prefs[Keys.NOTIF_ASR] ?: true,
            maghribEnabled = prefs[Keys.NOTIF_MAGHRIB] ?: true,
            ishaEnabled = prefs[Keys.NOTIF_ISHA] ?: true,
            minutesBefore = prefs[Keys.NOTIF_MIN_BEFORE] ?: 0,
            playSound = prefs[Keys.NOTIF_SOUND] ?: true
        )
    }

    val tasbihFlow: Flow<TasbihState> = context.dataStore.data.map { prefs ->
        val today = todayDateKey()
        val storedDate = prefs[Keys.TASBIH_DATE] ?: today
        val dailyCount = if (storedDate == today) (prefs[Keys.TASBIH_DAILY_COUNT] ?: 0) else 0
        TasbihState(
            selectedZikrId = prefs[Keys.TASBIH_ZIKR_ID] ?: "subhanallah",
            currentCount = prefs[Keys.TASBIH_CURRENT] ?: 0,
            roundTarget = prefs[Keys.TASBIH_TARGET] ?: 33,
            dailyGoal = prefs[Keys.TASBIH_DAILY_GOAL] ?: 100,
            dailyCount = dailyCount,
            lifetimeCount = prefs[Keys.TASBIH_LIFETIME] ?: 0L,
            lastDateKey = today
        )
    }

    val prayerTrackerFlow: Flow<Map<String, Set<PrayerName>>> = context.dataStore.data.map { prefs ->
        parseTrackerJson(prefs[Keys.TRACKER_JSON] ?: "{}")
    }

    val duaBookmarksFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        (prefs[Keys.DUA_BOOKMARKS] ?: "")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    suspend fun getSnapshot(): NamazSettingsState = settingsFlow.first()
    suspend fun getNotificationConfigSnapshot(): PrayerNotificationConfig = prayerNotificationsFlow.first()

    suspend fun setActive(active: Boolean, endTimeMillis: Long = 0L) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_ACTIVE] = active
            prefs[Keys.END_TIME] = if (active) endTimeMillis else 0L
        }
    }

    suspend fun saveSettings(updated: NamazSettingsState) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_CALL] = updated.autoCallResponseEnabled
            prefs[Keys.STRATEGY] = updated.callStrategy.name
            prefs[Keys.VOICE_LANG] = updated.voiceLanguage.name
            prefs[Keys.MSG_PRESET] = updated.messagePreset.name
            prefs[Keys.CUSTOM_BN] = updated.customMessageBn
            prefs[Keys.CUSTOM_EN] = updated.customMessageEn
            prefs[Keys.DURATION_MIN] = updated.durationMinutes
            prefs[Keys.AUTO_OFF] = updated.autoTurnOff
            prefs[Keys.APP_LANG] = updated.appLanguage.name
            prefs[Keys.CALC_METHOD] = updated.calculationMethod.name
            prefs[Keys.ASR_SCHOOL] = updated.asrSchool.name
            prefs[Keys.TOUCH_SOUNDS] = updated.touchSoundsEnabled
            prefs[Keys.HAPTIC_FEEDBACK] = updated.hapticFeedbackEnabled
            prefs[Keys.SOUND_VOLUME] = updated.soundVolume.coerceIn(0f, 1f)
            prefs[Keys.HAPTIC_INTENSITY] = updated.hapticIntensity.name
        }
    }

    suspend fun updateTouchAndSound(
        touchSoundsEnabled: Boolean,
        hapticFeedbackEnabled: Boolean,
        soundVolume: Float,
        hapticIntensity: HapticIntensity
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOUCH_SOUNDS] = touchSoundsEnabled
            prefs[Keys.HAPTIC_FEEDBACK] = hapticFeedbackEnabled
            prefs[Keys.SOUND_VOLUME] = soundVolume.coerceIn(0f, 1f)
            prefs[Keys.HAPTIC_INTENSITY] = hapticIntensity.name
        }
    }

    suspend fun saveLocation(latitude: Double, longitude: Double, locationName: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAS_LOCATION] = true
            prefs[Keys.LATITUDE] = latitude
            prefs[Keys.LONGITUDE] = longitude
            prefs[Keys.LOCATION_NAME] = locationName
        }
    }

    suspend fun toggleAutoCall(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_CALL] = enabled
        }
    }

    suspend fun setDurationMinutes(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DURATION_MIN] = minutes
        }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.APP_LANG] = language.name
        }
    }

    suspend fun savePrayerNotifications(config: PrayerNotificationConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIF_ENABLED] = config.enabled
            prefs[Keys.NOTIF_FAJR] = config.fajrEnabled
            prefs[Keys.NOTIF_DHUHR] = config.dhuhrEnabled
            prefs[Keys.NOTIF_ASR] = config.asrEnabled
            prefs[Keys.NOTIF_MAGHRIB] = config.maghribEnabled
            prefs[Keys.NOTIF_ISHA] = config.ishaEnabled
            prefs[Keys.NOTIF_MIN_BEFORE] = config.minutesBefore
            prefs[Keys.NOTIF_SOUND] = config.playSound
        }
    }

    suspend fun saveTasbihState(state: TasbihState) {
        val today = todayDateKey()
        context.dataStore.edit { prefs ->
            prefs[Keys.TASBIH_ZIKR_ID] = state.selectedZikrId
            prefs[Keys.TASBIH_CURRENT] = state.currentCount
            prefs[Keys.TASBIH_TARGET] = state.roundTarget
            prefs[Keys.TASBIH_DAILY_GOAL] = state.dailyGoal
            prefs[Keys.TASBIH_DAILY_COUNT] = state.dailyCount
            prefs[Keys.TASBIH_LIFETIME] = state.lifetimeCount
            prefs[Keys.TASBIH_DATE] = today
        }
    }

    suspend fun togglePrayerTracked(dateKey: String, prayer: PrayerName) {
        context.dataStore.edit { prefs ->
            val map = parseTrackerJson(prefs[Keys.TRACKER_JSON] ?: "{}").toMutableMap()
            val set = (map[dateKey] ?: emptySet()).toMutableSet()
            if (set.contains(prayer)) {
                set.remove(prayer)
            } else {
                set.add(prayer)
            }
            if (set.isEmpty()) {
                map.remove(dateKey)
            } else {
                map[dateKey] = set
            }
            prefs[Keys.TRACKER_JSON] = encodeTrackerJson(map)
        }
    }

    suspend fun toggleDuaBookmark(duaId: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[Keys.DUA_BOOKMARKS] ?: "")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toMutableSet()
            if (current.contains(duaId)) current.remove(duaId) else current.add(duaId)
            prefs[Keys.DUA_BOOKMARKS] = current.joinToString(",")
        }
    }

    private fun parseTrackerJson(raw: String): Map<String, Set<PrayerName>> {
        val out = linkedMapOf<String, Set<PrayerName>>()
        runCatching {
            val obj = JSONObject(raw)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val dateKey = keys.next()
                val value = obj.optString(dateKey, "")
                val prayers = value.split(",")
                    .mapNotNull { token ->
                        runCatching { PrayerName.valueOf(token.trim()) }.getOrNull()
                    }
                    .toSet()
                if (prayers.isNotEmpty()) {
                    out[dateKey] = prayers
                }
            }
        }
        return out
    }

    private fun encodeTrackerJson(map: Map<String, Set<PrayerName>>): String {
        val obj = JSONObject()
        // Keep up to 180 most recent days to keep preferences lean
        map.entries.sortedByDescending { it.key }.take(180).forEach { (dateKey, set) ->
            obj.put(dateKey, set.joinToString(",") { it.name })
        }
        return obj.toString()
    }

    companion object {
        private val _lastCallEvent = MutableStateFlow(CallEventInfo())
        val lastCallEvent: StateFlow<CallEventInfo> = _lastCallEvent.asStateFlow()

        fun publishCallEvent(event: CallEventInfo) {
            _lastCallEvent.value = event
        }

        fun clearActiveCallEvent() {
            _lastCallEvent.value = _lastCallEvent.value.copy(isActive = false)
        }

        fun todayDateKey(timeMillis: Long = System.currentTimeMillis()): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(timeMillis))
        }
    }
}
