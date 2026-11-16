package com.example.util

import com.example.data.AsrJuristicSchool
import com.example.data.DailyPrayerTimes
import com.example.data.PrayerCalculationMethod
import com.example.data.PrayerName
import com.example.data.PrayerTimeEntry
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.tan

class PrayerTimeCalculator {

    fun calculate(
        latitude: Double,
        longitude: Double,
        locationLabel: String = "",
        method: PrayerCalculationMethod = PrayerCalculationMethod.KARACHI,
        asrSchool: AsrJuristicSchool = AsrJuristicSchool.HANAFI,
        calendar: Calendar = Calendar.getInstance()
    ): DailyPrayerTimes = calculateForDate(
        latitude = latitude,
        longitude = longitude,
        locationLabel = locationLabel,
        method = method,
        asrSchool = asrSchool,
        calendar = calendar
    )

    companion object {
        fun calculateForDate(
            latitude: Double,
            longitude: Double,
            locationLabel: String,
            method: PrayerCalculationMethod,
            asrSchool: AsrJuristicSchool,
            calendar: Calendar = Calendar.getInstance()
        ): DailyPrayerTimes {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val tz = calendar.timeZone ?: TimeZone.getDefault()
        val timeZoneHours = tz.getOffset(calendar.timeInMillis) / 3_600_000.0
        val julianDate = julianDay(year, month, day) - longitude / (15.0 * 24.0)

        // Initial estimates (fractions of a day)
        var fajr = sunAngleTime(julianDate, 5.0 / 24.0, latitude, method.fajrAngle, timeZoneHours, longitude, counterClockwise = true)
        var sunrise = sunAngleTime(julianDate, 6.0 / 24.0, latitude, 0.833, timeZoneHours, longitude, counterClockwise = true)
        var dhuhr = midDay(julianDate, 12.0 / 24.0, timeZoneHours, longitude)
        var asr = asrTime(julianDate, 13.0 / 24.0, latitude, asrSchool.shadowFactor, timeZoneHours, longitude)
        var maghrib = sunAngleTime(julianDate, 18.0 / 24.0, latitude, 0.833, timeZoneHours, longitude, counterClockwise = false)
        var isha = if (method.ishaMinutesAfterMaghrib != null) {
            maghrib + method.ishaMinutesAfterMaghrib / 60.0
        } else {
            sunAngleTime(julianDate, 19.0 / 24.0, latitude, method.ishaAngle, timeZoneHours, longitude, counterClockwise = false)
        }

        // Second-pass refinement at computed prayer hours for higher accuracy
        fajr = sunAngleTime(julianDate, fajr / 24.0, latitude, method.fajrAngle, timeZoneHours, longitude, counterClockwise = true)
        sunrise = sunAngleTime(julianDate, sunrise / 24.0, latitude, 0.833, timeZoneHours, longitude, counterClockwise = true)
        dhuhr = midDay(julianDate, dhuhr / 24.0, timeZoneHours, longitude) + (1.0 / 60.0) // +1m precautionary zenith buffer
        asr = asrTime(julianDate, asr / 24.0, latitude, asrSchool.shadowFactor, timeZoneHours, longitude)
        maghrib = sunAngleTime(julianDate, maghrib / 24.0, latitude, 0.833, timeZoneHours, longitude, counterClockwise = false)
        isha = if (method.ishaMinutesAfterMaghrib != null) {
            maghrib + method.ishaMinutesAfterMaghrib / 60.0
        } else {
            sunAngleTime(julianDate, isha / 24.0, latitude, method.ishaAngle, timeZoneHours, longitude, counterClockwise = false)
        }

        val (srH, srM) = toHourMinute(sunrise)
        val sunriseFormatted = format12Hour(srH, srM)

        val rawTimes = listOf(
            PrayerName.FAJR to fajr,
            PrayerName.DHUHR to dhuhr,
            PrayerName.ASR to asr,
            PrayerName.MAGHRIB to maghrib,
            PrayerName.ISHA to isha
        )

        val nowMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val nowSecond = calendar.get(Calendar.SECOND)
        val parsed = rawTimes.map { (name, decHours) ->
            val (h, m) = toHourMinute(decHours)
            Triple(name, h, m)
        }

        val nextEntryToday = parsed.firstOrNull { (_, h, m) ->
            val entryMinutes = h * 60 + m
            entryMinutes > nowMinutes || (entryMinutes == nowMinutes && nowSecond == 0)
        }
        val nextPrayer = nextEntryToday?.first ?: PrayerName.FAJR

        val targetCal = (calendar.clone() as Calendar).apply {
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (nextEntryToday != null) {
                set(Calendar.HOUR_OF_DAY, nextEntryToday.second)
                set(Calendar.MINUTE, nextEntryToday.third)
            } else {
                add(Calendar.DAY_OF_MONTH, 1)
                val tomorrowTimes = calculateRawFajrHourMinute(
                    latitude = latitude,
                    longitude = longitude,
                    method = method,
                    calendar = this
                )
                set(Calendar.HOUR_OF_DAY, tomorrowTimes.first)
                set(Calendar.MINUTE, tomorrowTimes.second)
            }
        }

        val entries = parsed.map { (name, h, m) ->
            PrayerTimeEntry(
                prayer = name,
                hour24 = h,
                minute = m,
                formattedTime = format12Hour(h, m),
                isNext = (name == nextPrayer)
            )
        }

        return DailyPrayerTimes(
            locationLabel = locationLabel,
            entries = entries,
            nextPrayer = nextPrayer,
            nextPrayerEpochMillis = targetCal.timeInMillis,
            sunriseFormatted = sunriseFormatted
        )
    }

    private fun calculateRawFajrHourMinute(
        latitude: Double,
        longitude: Double,
        method: PrayerCalculationMethod,
        calendar: Calendar
    ): Pair<Int, Int> {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val tz = calendar.timeZone ?: TimeZone.getDefault()
        val timeZoneHours = tz.getOffset(calendar.timeInMillis) / 3_600_000.0
        val jd = julianDay(year, month, day) - longitude / (15.0 * 24.0)
        val fajrPass1 = sunAngleTime(jd, 5.0 / 24.0, latitude, method.fajrAngle, timeZoneHours, longitude, counterClockwise = true)
        val fajrPass2 = sunAngleTime(jd, fajrPass1 / 24.0, latitude, method.fajrAngle, timeZoneHours, longitude, counterClockwise = true)
        return toHourMinute(fajrPass2)
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2 * g))
        val e = 23.439 - 0.00000036 * d
        val ra = atan2Deg(cosDeg(e) * sinDeg(l), cosDeg(l)) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = asinDeg(sinDeg(e) * sinDeg(l))
        return decl to eqt
    }

    private fun midDay(jd: Double, t: Double, tzHours: Double, longitude: Double): Double {
        val (_, eqt) = sunPosition(jd + t)
        return fixHour(12.0 + tzHours - longitude / 15.0 - eqt)
    }

    private fun sunAngleTime(
        jd: Double,
        t: Double,
        latitude: Double,
        angle: Double,
        tzHours: Double,
        longitude: Double,
        counterClockwise: Boolean
    ): Double {
        val (decl, _) = sunPosition(jd + t)
        val noon = midDay(jd, t, tzHours, longitude)
        val cosH = ((-sinDeg(angle) - sinDeg(decl) * sinDeg(latitude)) /
            (cosDeg(decl) * cosDeg(latitude))).coerceIn(-1.0, 1.0)
        val hourAngle = acosDeg(cosH) / 15.0
        return fixHour(noon + if (counterClockwise) -hourAngle else hourAngle)
    }

    private fun asrTime(
        jd: Double,
        t: Double,
        latitude: Double,
        factor: Double,
        tzHours: Double,
        longitude: Double
    ): Double {
        val (decl, _) = sunPosition(jd + t)
        val angle = -atanDeg(1.0 / (factor + tanDeg(abs(latitude - decl))))
        return sunAngleTime(jd, t, latitude, angle, tzHours, longitude, counterClockwise = false)
    }

    private fun toHourMinute(decHours: Double): Pair<Int, Int> {
        val normalized = fixHour(decHours + 0.5 / 60.0)
        val hours = floor(normalized).toInt() % 24
        val minutes = ((normalized - floor(normalized)) * 60.0).roundToInt() % 60
        return hours to minutes
    }

    private fun format12Hour(hour24: Int, minute: Int): String {
        val amPm = if (hour24 < 12) "AM" else "PM"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return String.format(Locale.US, "%d:%02d %s", hour12, minute, amPm)
    }

    private fun fixAngle(a: Double): Double = fix(a, 360.0)
    private fun fixHour(a: Double): Double = fix(a, 24.0)

    private fun fix(a: Double, b: Double): Double {
        var res = a - b * floor(a / b)
        if (res < 0) res += b
        return res
    }

    private fun sinDeg(d: Double) = sin(Math.toRadians(d))
    private fun cosDeg(d: Double) = cos(Math.toRadians(d))
    private fun tanDeg(d: Double) = tan(Math.toRadians(d))
    private fun asinDeg(x: Double) = Math.toDegrees(asin(x.coerceIn(-1.0, 1.0)))
    private fun acosDeg(x: Double) = Math.toDegrees(acos(x.coerceIn(-1.0, 1.0)))
    private fun atanDeg(x: Double) = Math.toDegrees(atan(x))
    private fun atan2Deg(y: Double, x: Double) = Math.toDegrees(atan2(y, x))
    }
}
