package com.example.util

import android.os.Build
import com.example.data.AppLanguage
import com.example.data.AsrJuristicSchool
import com.example.data.PrayerCalculationMethod
import com.example.data.PrayerName
import com.example.data.RamadanDaySchedule
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Calendar
import java.util.Locale
import kotlin.math.floor

data class HijriDateInfo(
    val year: Int,
    val month: Int,
    val day: Int,
    val monthNameEn: String,
    val monthNameBn: String
) {
    val isRamadan: Boolean get() = month == 9

    fun formatted(language: AppLanguage): String {
        val monthLabel = if (language == AppLanguage.BENGALI) monthNameBn else monthNameEn
        return "$day $monthLabel $year AH"
    }
}

object HijriDateHelper {

    private val monthsEn = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val monthsBn = listOf(
        "মুহররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি",
        "জুমাদাল উলা", "জুমাদাস সানি", "রজব", "শাবান",
        "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ"
    )

    fun fromCalendar(calendar: Calendar = Calendar.getInstance()): HijriDateInfo {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val local = LocalDate.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            val hijri = HijrahDate.from(local)
            val y = hijri.get(ChronoField.YEAR)
            val m = hijri.get(ChronoField.MONTH_OF_YEAR).coerceIn(1, 12)
            val d = hijri.get(ChronoField.DAY_OF_MONTH)
            return HijriDateInfo(
                year = y,
                month = m,
                day = d,
                monthNameEn = monthsEn[m - 1],
                monthNameBn = monthsBn[m - 1]
            )
        }
        return tabularGregorianToHijri(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun generateRamadanCalendar(
        latitude: Double,
        longitude: Double,
        method: PrayerCalculationMethod,
        asrSchool: AsrJuristicSchool,
        today: Calendar = Calendar.getInstance()
    ): Pair<Boolean, List<RamadanDaySchedule>> {
        val hijriToday = fromCalendar(today)
        val isCurrentlyRamadan = hijriToday.isRamadan
        val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.US)

        // Find start date of current Ramadan (if in Ramadan) or next Ramadan 1st
        val startCal = (today.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (isCurrentlyRamadan) {
            startCal.add(Calendar.DAY_OF_MONTH, -(hijriToday.day - 1))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val targetYear = if (hijriToday.month > 9) hijriToday.year + 1 else hijriToday.year
            val ramadan1st = HijrahChronology.INSTANCE.date(targetYear, 9, 1)
            val gDate = LocalDate.from(ramadan1st)
            startCal.set(gDate.year, gDate.monthValue - 1, gDate.dayOfMonth)
        } else {
            // Walk forward day by day up to 360 days until month == 9 && day == 1
            val probe = startCal.clone() as Calendar
            for (offset in 0..360) {
                val info = fromCalendar(probe)
                if (info.month == 9 && info.day == 1) {
                    startCal.timeInMillis = probe.timeInMillis
                    break
                }
                probe.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(today.time)
        val dayKeyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val rows = (1..30).map { dayIndex ->
            val dayCal = (startCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, dayIndex - 1)
            }
            val times = PrayerTimeCalculator.calculateForDate(
                latitude = latitude,
                longitude = longitude,
                locationLabel = "",
                method = method,
                asrSchool = asrSchool,
                calendar = dayCal
            )
            val fajr = times.entries.first { it.prayer == PrayerName.FAJR }
            val maghrib = times.entries.first { it.prayer == PrayerName.MAGHRIB }
            RamadanDaySchedule(
                ramadanDay = dayIndex,
                gregorianLabel = dateFormat.format(dayCal.time),
                sehriTime = fajr.formattedTime,
                fajrTime = fajr.formattedTime,
                iftarTime = maghrib.formattedTime,
                isToday = dayKeyFmt.format(dayCal.time) == todayKey
            )
        }

        return isCurrentlyRamadan to rows
    }

    private fun tabularGregorianToHijri(year: Int, month: Int, day: Int): HijriDateInfo {
        val jd = if ((year > 1582) || ((year == 1582) && (month > 10)) || ((year == 1582) && (month == 10) && (day > 14))) {
            ((1461 * (year + 4800 + (month - 14) / 12)) / 4 +
                (367 * (month - 2 - 12 * ((month - 14) / 12))) / 12 -
                (3 * ((year + 4900 + (month - 14) / 12) / 100)) / 4 + day - 32075)
        } else {
            (367 * year - (7 * (year + 5001 + (month - 9) / 7)) / 4 + (275 * month) / 9 + day + 1729777)
        }
        val l = jd - 1948440 + 10632
        val n = (l - 1) / 10631
        val l2 = l - 10631 * n + 354
        val j = ((10985 - l2) / 5316) * ((50 * l2) / 17719) + (l2 / 5670) * ((43 * l2) / 15238)
        val l3 = l2 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val m = ((24 * l3) / 709).coerceIn(1, 12)
        val d = l3 - (709 * m) / 24
        val y = 30 * n + j - 30
        return HijriDateInfo(
            year = y,
            month = m,
            day = d,
            monthNameEn = monthsEn[m - 1],
            monthNameBn = monthsBn[m - 1]
        )
    }
}
