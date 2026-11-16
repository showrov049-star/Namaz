package com.example

import com.example.data.AsrJuristicSchool
import com.example.data.PrayerCalculationMethod
import com.example.data.PrayerName
import com.example.util.PrayerTimeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class PrayerTimeCalculatorTest {

    @Test
    fun calculatesOrderedPrayerTimesForDhaka() {
        val tz = TimeZone.getTimeZone("Asia/Dhaka")
        val cal = Calendar.getInstance(tz).apply {
            set(2026, Calendar.NOVEMBER, 16, 10, 0, 0)
        }
        val result = PrayerTimeCalculator.calculateForDate(
            latitude = 23.8103,
            longitude = 90.4125,
            locationLabel = "Dhaka, BD",
            method = PrayerCalculationMethod.KARACHI,
            asrSchool = AsrJuristicSchool.HANAFI,
            calendar = cal
        )

        assertEquals(5, result.entries.size)
        val minutes = result.entries.map { it.hour24 * 60 + it.minute }
        for (i in 0 until minutes.lastIndex) {
            assertTrue("Expected prayer $i to be before ${i + 1}", minutes[i] < minutes[i + 1])
        }
        // Fajr in Dhaka in mid-November should be around 4:40 - 5:15 AM
        val fajrMinutes = minutes[0]
        assertTrue("Fajr out of range: $fajrMinutes", fajrMinutes in (4 * 60 + 30)..(5 * 60 + 30))
        // Next prayer at 10:00 AM should be Dhuhr
        assertEquals(PrayerName.DHUHR, result.nextPrayer)
    }

    @Test
    fun hanafiAsrIsLaterThanStandardAsr() {
        val tz = TimeZone.getTimeZone("Asia/Dhaka")
        val cal = Calendar.getInstance(tz).apply {
            set(2026, Calendar.NOVEMBER, 16, 10, 0, 0)
        }
        val hanafi = PrayerTimeCalculator.calculateForDate(
            latitude = 23.8103,
            longitude = 90.4125,
            locationLabel = "Dhaka, BD",
            method = PrayerCalculationMethod.KARACHI,
            asrSchool = AsrJuristicSchool.HANAFI,
            calendar = cal
        )
        val standard = PrayerTimeCalculator.calculateForDate(
            latitude = 23.8103,
            longitude = 90.4125,
            locationLabel = "Dhaka, BD",
            method = PrayerCalculationMethod.KARACHI,
            asrSchool = AsrJuristicSchool.STANDARD,
            calendar = cal
        )
        val hanafiAsr = hanafi.entries.first { it.prayer == PrayerName.ASR }
        val standardAsr = standard.entries.first { it.prayer == PrayerName.ASR }
        assertTrue(
            (hanafiAsr.hour24 * 60 + hanafiAsr.minute) >
                (standardAsr.hour24 * 60 + standardAsr.minute)
        )
    }
}
