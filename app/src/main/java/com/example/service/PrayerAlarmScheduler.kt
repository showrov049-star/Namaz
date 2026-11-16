package com.example.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.NamazSettingsState
import com.example.data.PrayerName
import com.example.data.PrayerNotificationConfig
import com.example.receiver.PrayerNotificationReceiver
import com.example.util.PrayerTimeCalculator
import java.util.Calendar

object PrayerAlarmScheduler {

    const val ACTION_PRAYER_REMINDER = "com.example.ACTION_PRAYER_REMINDER"
    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"

    fun rescheduleAll(
        context: Context,
        settings: NamazSettingsState,
        config: PrayerNotificationConfig
    ) {
        cancelAll(context)
        if (!config.enabled || !settings.hasSavedLocation) return

        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val now = System.currentTimeMillis()

        val todayCal = Calendar.getInstance()
        val tomorrowCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }

        val todayTimes = PrayerTimeCalculator.calculateForDate(
            latitude = settings.latitude,
            longitude = settings.longitude,
            locationLabel = settings.locationName,
            method = settings.calculationMethod,
            asrSchool = settings.asrSchool,
            calendar = todayCal
        )
        val tomorrowTimes = PrayerTimeCalculator.calculateForDate(
            latitude = settings.latitude,
            longitude = settings.longitude,
            locationLabel = settings.locationName,
            method = settings.calculationMethod,
            asrSchool = settings.asrSchool,
            calendar = tomorrowCal
        )

        PrayerName.entries.forEach { prayer ->
            if (!config.isPrayerEnabled(prayer)) return@forEach

            val todayEntry = todayTimes.entries.firstOrNull { it.prayer == prayer } ?: return@forEach
            val triggerToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, todayEntry.hour24)
                set(Calendar.MINUTE, todayEntry.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, -config.minutesBefore)
            }.timeInMillis

            val triggerMillis = if (triggerToday > now + 5_000L) {
                triggerToday
            } else {
                val tmrwEntry = tomorrowTimes.entries.firstOrNull { it.prayer == prayer } ?: return@forEach
                (tomorrowCal.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, tmrwEntry.hour24)
                    set(Calendar.MINUTE, tmrwEntry.minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.MINUTE, -config.minutesBefore)
                }.timeInMillis
            }

            val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                action = ACTION_PRAYER_REMINDER
                putExtra(EXTRA_PRAYER_NAME, prayer.name)
                putExtra(EXTRA_MINUTES_BEFORE, config.minutesBefore)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                requestCodeFor(prayer),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            scheduleAlarmCompat(alarmManager, triggerMillis, pending)
        }
    }

    @SuppressLint("MissingPermission")
    private fun scheduleAlarmCompat(
        alarmManager: AlarmManager,
        triggerMillis: Long,
        pending: PendingIntent
    ) {
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        if (canExact) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
                return
            } catch (_: SecurityException) {
                // Fall back to inexact allow-while-idle below
            }
        }
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pending)
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        PrayerName.entries.forEach { prayer ->
            val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
                action = ACTION_PRAYER_REMINDER
            }
            val pending = PendingIntent.getBroadcast(
                context,
                requestCodeFor(prayer),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pending != null) {
                alarmManager.cancel(pending)
                pending.cancel()
            }
        }
    }

    private fun requestCodeFor(prayer: PrayerName): Int = 4000 + prayer.ordinal
}
