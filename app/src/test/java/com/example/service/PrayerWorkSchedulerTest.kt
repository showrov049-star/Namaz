package com.example.service

import com.example.data.NamazSettingsState
import com.example.data.PrayerName
import com.example.data.PrayerNotificationConfig
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class PrayerWorkSchedulerTest {

    @Test
    fun `computeTriggerMillisForPrayer returns future trigger when enabled with location`() {
        val settings = NamazSettingsState(
            hasSavedLocation = true,
            latitude = 23.8103,
            longitude = 90.4125,
            locationName = "Dhaka"
        )
        val config = PrayerNotificationConfig(
            enabled = true,
            fajrEnabled = true,
            dhuhrEnabled = true,
            asrEnabled = false,
            maghribEnabled = true,
            ishaEnabled = true,
            minutesBefore = 10
        )

        val noonNow = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val maghribTrigger = PrayerWorkScheduler.computeTriggerMillisForPrayer(
            PrayerName.MAGHRIB,
            settings,
            config,
            noonNow
        )
        assertNotNull(maghribTrigger)
        assertTrue((maghribTrigger ?: 0L) > noonNow)

        val asrTrigger = PrayerWorkScheduler.computeTriggerMillisForPrayer(
            PrayerName.ASR,
            settings,
            config,
            noonNow
        )
        assertNull(asrTrigger)
    }

    @Test
    fun `computeTriggerMillisForPrayer returns null when disabled`() {
        val settings = NamazSettingsState(hasSavedLocation = true, latitude = 23.8103, longitude = 90.4125)
        val config = PrayerNotificationConfig(enabled = false)
        assertNull(PrayerWorkScheduler.computeTriggerMillisForPrayer(PrayerName.FAJR, settings, config))
    }
}
