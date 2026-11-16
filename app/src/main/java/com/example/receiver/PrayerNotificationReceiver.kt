package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppLanguage
import com.example.data.NamazRepository
import com.example.data.PrayerName
import com.example.service.PrayerAlarmScheduler
import com.example.util.AppStrings
import com.example.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrayerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = NamazRepository(context)
                val settings = repo.getSnapshot()
                val notifConfig = repo.getNotificationConfigSnapshot()

                if (action == PrayerAlarmScheduler.ACTION_PRAYER_REMINDER && notifConfig.enabled) {
                    val prayerRaw = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME)
                    val prayer = runCatching { PrayerName.valueOf(prayerRaw ?: "") }.getOrNull()
                    val minsBefore = intent.getIntExtra(PrayerAlarmScheduler.EXTRA_MINUTES_BEFORE, 0)
                    if (prayer != null && notifConfig.isPrayerEnabled(prayer)) {
                        postPrayerNotification(
                            context = context,
                            prayer = prayer,
                            minutesBefore = minsBefore,
                            language = settings.appLanguage,
                            playSound = notifConfig.playSound
                        )
                    }
                }

                // Always reschedule next upcoming prayer alarms & WorkManager reminders (on boot or after firing)
                PrayerAlarmScheduler.rescheduleAll(context, settings, notifConfig)
                com.example.service.PrayerWorkScheduler.rescheduleAll(context, settings, notifConfig)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val CHANNEL_ID_SOUND = "prayer_times_channel_sound"
        private const val CHANNEL_ID_SILENT = "prayer_times_channel_silent"

        fun postTestNotification(context: Context, language: AppLanguage, playSound: Boolean) {
            postPrayerNotification(
                context = context,
                prayer = PrayerName.MAGHRIB,
                minutesBefore = 0,
                language = language,
                playSound = playSound,
                isTest = true
            )
        }

        fun postPrayerNotification(
            context: Context,
            prayer: PrayerName,
            minutesBefore: Int,
            language: AppLanguage,
            playSound: Boolean,
            isTest: Boolean = false
        ) {
            if (!PermissionHelper.hasNotificationPermission(context)) return

            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val channelId = if (playSound) CHANNEL_ID_SOUND else CHANNEL_ID_SILENT
            ensureChannels(manager)

            val s = AppStrings.get(language)
            val prayerLabel = s.prayerLabel(prayer)
            val title = if (isTest) {
                if (language == AppLanguage.BENGALI) "নামাজ রিমাইন্ডার টেস্ট" else "Prayer Reminder Test"
            } else if (minutesBefore > 0) {
                if (language == AppLanguage.BENGALI) {
                    "$prayerLabel-এর $minutesBefore মিনিট বাকি"
                } else {
                    "$prayerLabel in $minutesBefore min"
                }
            } else {
                if (language == AppLanguage.BENGALI) {
                    "$prayerLabel-এর সময় হয়েছে"
                } else {
                    "Time for $prayerLabel Prayer"
                }
            }

            val body = if (language == AppLanguage.BENGALI) {
                "প্রস্তুতি নিন এবং প্রশান্তির সাথে সালাত আদায় করুন।"
            } else {
                "Prepare for Salah and activate Namaz Mode for peaceful focus."
            }

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openPending = PendingIntent.getActivity(
                context,
                4900,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_namaz_tile)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(openPending)
                .build()

            manager.notify(4100 + prayer.ordinal, notification)
        }

        private fun ensureChannels(manager: NotificationManager) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val soundChannel = NotificationChannel(
                    CHANNEL_ID_SOUND,
                    "Prayer Time Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Scheduled daily Salah reminders"
                }
                val silentChannel = NotificationChannel(
                    CHANNEL_ID_SILENT,
                    "Prayer Time Alerts (Silent)",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Silent scheduled daily Salah reminders"
                    setSound(null, null)
                    enableVibration(false)
                }
                manager.createNotificationChannel(soundChannel)
                manager.createNotificationChannel(silentChannel)
            }
        }
    }
}
