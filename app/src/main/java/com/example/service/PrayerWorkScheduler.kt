package com.example.service

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.NamazSettingsState
import com.example.data.PrayerName
import com.example.data.PrayerNotificationConfig
import com.example.util.PrayerTimeCalculator
import java.util.Calendar
import java.util.concurrent.TimeUnit

object PrayerWorkScheduler {

    private const val TAG_PRAYER_WORK = "namaz_prayer_work"
    private const val UNIQUE_PERIODIC_RESCHEDULER = "namaz_prayer_daily_rescheduler"

    fun uniqueWorkName(prayer: PrayerName): String = "namaz_prayer_alert_${prayer.name.lowercase()}"

    fun computeTriggerMillisForPrayer(
        prayer: PrayerName,
        settings: NamazSettingsState,
        config: PrayerNotificationConfig,
        nowMillis: Long = System.currentTimeMillis()
    ): Long? {
        if (!config.enabled || !settings.hasSavedLocation || !config.isPrayerEnabled(prayer)) {
            return null
        }

        val todayCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
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

        val todayEntry = todayTimes.entries.firstOrNull { it.prayer == prayer } ?: return null
        val triggerToday = (todayCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, todayEntry.hour24)
            set(Calendar.MINUTE, todayEntry.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -config.minutesBefore)
        }.timeInMillis

        if (triggerToday > nowMillis + 5_000L) {
            return triggerToday
        }

        val tomorrowEntry = tomorrowTimes.entries.firstOrNull { it.prayer == prayer } ?: return null
        return (tomorrowCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, tomorrowEntry.hour24)
            set(Calendar.MINUTE, tomorrowEntry.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, -config.minutesBefore)
        }.timeInMillis
    }

    fun rescheduleAll(
        context: Context,
        settings: NamazSettingsState,
        config: PrayerNotificationConfig
    ) {
        val workManager = runCatching { WorkManager.getInstance(context) }.getOrNull() ?: return

        if (!config.enabled || !settings.hasSavedLocation) {
            cancelAll(workManager)
            return
        }

        val now = System.currentTimeMillis()

        PrayerName.entries.forEach { prayer ->
            val workName = uniqueWorkName(prayer)
            val triggerMillis = computeTriggerMillisForPrayer(prayer, settings, config, now)
            if (triggerMillis == null) {
                workManager.cancelUniqueWork(workName)
            } else {
                val delayMillis = (triggerMillis - now).coerceAtLeast(1_000L)
                val inputData = Data.Builder()
                    .putString(PrayerReminderWorker.KEY_PRAYER_NAME, prayer.name)
                    .putInt(PrayerReminderWorker.KEY_MINUTES_BEFORE, config.minutesBefore)
                    .build()

                val request = OneTimeWorkRequestBuilder<PrayerReminderWorker>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag(TAG_PRAYER_WORK)
                    .build()

                workManager.enqueueUniqueWork(
                    workName,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
            }
        }

        val periodicRefresh = PeriodicWorkRequestBuilder<PrayerRescheduleWorker>(
            12,
            TimeUnit.HOURS
        )
            .addTag(TAG_PRAYER_WORK)
            .build()

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC_RESCHEDULER,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRefresh
        )
    }

    fun cancelAll(context: Context) {
        val workManager = runCatching { WorkManager.getInstance(context) }.getOrNull() ?: return
        cancelAll(workManager)
    }

    private fun cancelAll(workManager: WorkManager) {
        PrayerName.entries.forEach { prayer ->
            workManager.cancelUniqueWork(uniqueWorkName(prayer))
        }
        workManager.cancelUniqueWork(UNIQUE_PERIODIC_RESCHEDULER)
        workManager.cancelAllWorkByTag(TAG_PRAYER_WORK)
    }
}
