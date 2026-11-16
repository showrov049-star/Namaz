package com.example

import com.example.data.AsrJuristicSchool
import com.example.data.IslamicContentData
import com.example.data.PrayerCalculationMethod
import com.example.util.HijriDateHelper
import com.example.util.PrayerTimeCalculator
import com.example.util.QiblaCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class LifestyleModulesTest {

    @Test
    fun `qibla bearing for Dhaka is west-northwest around 277 degrees`() {
        val details = QiblaCalculator.calculate(23.8103, 90.4125)
        assertTrue(details.bearingDegrees in 275f..280f)
        assertTrue(details.distanceKm in 5000.0..5400.0)
        assertEquals("W", details.cardinalDirection)
    }

    @Test
    fun `prayer calculator returns next prayer epoch millis in future and sunrise`() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Dhaka")).apply {
            set(2026, Calendar.MARCH, 15, 10, 0, 0)
        }
        val times = PrayerTimeCalculator.calculateForDate(
            latitude = 23.8103,
            longitude = 90.4125,
            locationLabel = "Dhaka",
            method = PrayerCalculationMethod.KARACHI,
            asrSchool = AsrJuristicSchool.HANAFI,
            calendar = cal
        )
        assertTrue(times.sunriseFormatted.isNotBlank())
        assertTrue(times.nextPrayerEpochMillis > cal.timeInMillis)
    }

    @Test
    fun `ramadan calendar generates 30 valid days for user location`() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Dhaka")).apply {
            set(2026, Calendar.FEBRUARY, 18, 12, 0, 0)
        }
        val (_, days) = HijriDateHelper.generateRamadanCalendar(
            latitude = 23.8103,
            longitude = 90.4125,
            method = PrayerCalculationMethod.KARACHI,
            asrSchool = AsrJuristicSchool.HANAFI,
            today = cal
        )
        assertEquals(30, days.size)
        assertEquals(1, days.first().ramadanDay)
        assertEquals(30, days.last().ramadanDay)
        assertTrue(days.all { it.sehriTime.isNotBlank() && it.iftarTime.isNotBlank() })
    }

    @Test
    fun `verified offline surahs, zikr options, and daily duas are populated`() {
        assertTrue(IslamicContentData.offlineSurahs.isNotEmpty())
        assertEquals(7, IslamicContentData.offlineSurahs.first().ayahs.size)
        assertTrue(IslamicContentData.zikrOptions.size >= 5)
        assertTrue(IslamicContentData.dailyDuas.size >= 6)
    }
}
