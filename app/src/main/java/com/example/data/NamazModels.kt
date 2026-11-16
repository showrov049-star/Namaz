package com.example.data

enum class AppLanguage(val label: String) {
    ENGLISH("English"),
    BENGALI("বাংলা (Bangla)")
}

enum class VoiceLanguage(val label: String, val localeTag: String) {
    BENGALI("বাংলা (Bangla)", "bn-BD"),
    ENGLISH("English", "en-US")
}

enum class VoiceMessagePreset {
    DEFAULT,
    CUSTOM
}

enum class CallHandlingStrategy(val titleEn: String, val titleBn: String) {
    SCREEN_AND_REJECT(
        "Screen & Silence/Reject (Recommended)",
        "কল স্ক্রিনিং ও সাইলেন্ট/রিজেক্ট (নির্ভরযোগ্য)"
    ),
    AUTO_ANSWER_AND_TTS(
        "Auto-Answer & Play TTS (Device Dependent)",
        "অটো-রিসিভ ও ভয়েস প্লে (ডিভাইস সাপেক্ষ)"
    )
}

enum class PrayerCalculationMethod(
    val titleEn: String,
    val titleBn: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutesAfterMaghrib: Int? = null
) {
    KARACHI(
        titleEn = "Univ. of Islamic Sciences, Karachi",
        titleBn = "করাচি বিশ্ববিদ্যালয় (দক্ষিণ এশিয়া)",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    MWL(
        titleEn = "Muslim World League",
        titleBn = "মুসলিম ওয়ার্ল্ড লিগ (MWL)",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    ISNA(
        titleEn = "ISNA (North America)",
        titleBn = "ইসনা (উত্তর আমেরিকা)",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    ),
    EGYPT(
        titleEn = "Egyptian General Authority",
        titleBn = "মিশরীয় জেনারেল অথরিটি",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    MAKKAH(
        titleEn = "Umm al-Qura, Makkah",
        titleBn = "উম্মুল কুরা, মক্কা",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaMinutesAfterMaghrib = 90
    )
}

enum class AsrJuristicSchool(val titleEn: String, val titleBn: String, val shadowFactor: Double) {
    HANAFI("Hanafi (2x shadow)", "হানাফি (২ গুণ ছায়া)", 2.0),
    STANDARD("Standard / Shafi'i (1x shadow)", "শাফেয়ী / স্ট্যান্ডার্ড (১ গুণ ছায়া)", 1.0)
}

enum class HapticIntensity(val titleEn: String, val titleBn: String, val scale: Float) {
    LIGHT("Light", "হালকা", 0.45f),
    MEDIUM("Medium", "মাঝারি", 0.72f),
    STRONG("Strong", "শক্তিশালী", 1.0f)
}

enum class PrayerName {
    FAJR, DHUHR, ASR, MAGHRIB, ISHA
}

data class PrayerTimeEntry(
    val prayer: PrayerName,
    val hour24: Int,
    val minute: Int,
    val formattedTime: String,
    val isNext: Boolean = false
)

data class DailyPrayerTimes(
    val locationLabel: String = "",
    val entries: List<PrayerTimeEntry> = emptyList(),
    val nextPrayer: PrayerName? = null,
    val nextPrayerEpochMillis: Long = 0L,
    val sunriseFormatted: String = ""
)

data class PrayerNotificationConfig(
    val enabled: Boolean = false,
    val fajrEnabled: Boolean = true,
    val dhuhrEnabled: Boolean = true,
    val asrEnabled: Boolean = true,
    val maghribEnabled: Boolean = true,
    val ishaEnabled: Boolean = true,
    val minutesBefore: Int = 0,
    val playSound: Boolean = true
) {
    fun isPrayerEnabled(prayer: PrayerName): Boolean = when (prayer) {
        PrayerName.FAJR -> fajrEnabled
        PrayerName.DHUHR -> dhuhrEnabled
        PrayerName.ASR -> asrEnabled
        PrayerName.MAGHRIB -> maghribEnabled
        PrayerName.ISHA -> ishaEnabled
    }
}

data class ZikrOption(
    val id: String,
    val arabic: String,
    val transliteration: String,
    val meaningEn: String,
    val meaningBn: String,
    val defaultTarget: Int
)

data class TasbihState(
    val selectedZikrId: String = "subhanallah",
    val currentCount: Int = 0,
    val roundTarget: Int = 33,
    val dailyGoal: Int = 100,
    val dailyCount: Int = 0,
    val lifetimeCount: Long = 0L,
    val lastDateKey: String = ""
)

data class PrayerTrackerDay(
    val dateKey: String,
    val completedPrayers: Set<PrayerName> = emptySet()
)

data class QuranSurahSummary(
    val number: Int,
    val arabicName: String,
    val englishName: String,
    val englishTranslation: String,
    val banglaName: String,
    val ayahCount: Int,
    val revelationType: String
)

data class QuranAyah(
    val numberInSurah: Int,
    val globalNumber: Int,
    val arabicText: String,
    val banglaTranslation: String,
    val transliteration: String = "",
    val audioUrl: String
)

data class QuranSurahDetail(
    val summary: QuranSurahSummary,
    val ayahs: List<QuranAyah>,
    val isOfflineVerified: Boolean = false
)

enum class DuaCategory(val titleEn: String, val titleBn: String) {
    ALL("All", "সকল"),
    MORNING_EVENING("Morning & Evening", "সকাল ও সন্ধ্যা"),
    SALAH("Prayer", "নামাজ"),
    DAILY_LIFE("Daily Life", "দৈনন্দিন"),
    FORGIVENESS("Forgiveness", "ক্ষমা প্রার্থনা")
}

data class DuaEntry(
    val id: String,
    val category: DuaCategory,
    val titleEn: String,
    val titleBn: String,
    val arabic: String,
    val transliteration: String,
    val translationBn: String,
    val translationEn: String,
    val reference: String
)

data class RamadanDaySchedule(
    val ramadanDay: Int,
    val gregorianLabel: String,
    val sehriTime: String,
    val fajrTime: String,
    val iftarTime: String,
    val isToday: Boolean = false
)

data class CallEventInfo(
    val callerNumber: String = "Unknown Number",
    val statusText: String = "",
    val spokenMessage: String = "",
    val isActive: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class NamazSettingsState(
    val isNamazModeActive: Boolean = false,
    val autoCallResponseEnabled: Boolean = false,
    val callStrategy: CallHandlingStrategy = CallHandlingStrategy.SCREEN_AND_REJECT,
    val voiceLanguage: VoiceLanguage = VoiceLanguage.BENGALI,
    val messagePreset: VoiceMessagePreset = VoiceMessagePreset.DEFAULT,
    val customMessageBn: String = DEFAULT_MESSAGE_BN,
    val customMessageEn: String = DEFAULT_MESSAGE_EN,
    val durationMinutes: Int = 15,
    val autoTurnOff: Boolean = true,
    val appLanguage: AppLanguage = AppLanguage.ENGLISH,
    val endTimeMillis: Long = 0L,
    val hasSavedLocation: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",
    val calculationMethod: PrayerCalculationMethod = PrayerCalculationMethod.KARACHI,
    val asrSchool: AsrJuristicSchool = AsrJuristicSchool.HANAFI,
    val touchSoundsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val soundVolume: Float = 0.75f,
    val hapticIntensity: HapticIntensity = HapticIntensity.MEDIUM
) {
    companion object {
        const val DEFAULT_MESSAGE_BN =
            "আপনি যাকে কল করেছেন, তিনি এখন নামাজ পড়ছেন। অনুগ্রহ করে নামাজ শেষ হওয়ার পর আবার কল করুন। ধন্যবাদ।"
        const val DEFAULT_MESSAGE_EN =
            "The person you are calling is currently performing Salah. Please call again after some time. Thank you."
    }

    fun resolveSpokenMessage(): String {
        return when (voiceLanguage) {
            VoiceLanguage.BENGALI ->
                if (messagePreset == VoiceMessagePreset.CUSTOM && customMessageBn.isNotBlank()) {
                    customMessageBn
                } else {
                    DEFAULT_MESSAGE_BN
                }
            VoiceLanguage.ENGLISH ->
                if (messagePreset == VoiceMessagePreset.CUSTOM && customMessageEn.isNotBlank()) {
                    customMessageEn
                } else {
                    DEFAULT_MESSAGE_EN
                }
        }
    }
}
