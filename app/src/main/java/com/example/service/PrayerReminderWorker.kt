package com.example.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.NamazRepository
import com.example.data.PrayerName
import com.example.receiver.PrayerNotificationReceiver

class PrayerReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repo = NamazRepository(applicationContext)
        val settings = repo.getSnapshot()
        val notifConfig = repo.getNotificationConfigSnapshot()

        if (notifConfig.enabled) {
            val prayerRaw = inputData.getString(KEY_PRAYER_NAME)
            val prayer = runCatching { PrayerName.valueOf(prayerRaw ?: "") }.getOrNull()
            val minutesBefore = inputData.getInt(KEY_MINUTES_BEFORE, notifConfig.minutesBefore)

            if (prayer != null && notifConfig.isPrayerEnabled(prayer)) {
                PrayerNotificationReceiver.postPrayerNotification(
                    context = applicationContext,
                    prayer = prayer,
                    minutesBefore = minutesBefore,
                    language = settings.appLanguage,
                    playSound = notifConfig.playSound
                )
            }
        }

        // Reschedule upcoming reminders cleanly so daily prayer alerts continue automatically
        PrayerWorkScheduler.rescheduleAll(applicationContext, settings, notifConfig)
        PrayerAlarmScheduler.rescheduleAll(applicationContext, settings, notifConfig)

        return Result.success()
    }

    companion object {
        const val KEY_PRAYER_NAME = "key_prayer_name"
        const val KEY_MINUTES_BEFORE = "key_minutes_before"
    }
}

class PrayerRescheduleWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repo = NamazRepository(applicationContext)
        val settings = repo.getSnapshot()
        val notifConfig = repo.getNotificationConfigSnapshot()
        PrayerWorkScheduler.rescheduleAll(applicationContext, settings, notifConfig)
        PrayerAlarmScheduler.rescheduleAll(applicationContext, settings, notifConfig)
        return Result.success()
    }
}
