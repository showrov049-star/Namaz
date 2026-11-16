package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.NamazRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NamazForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var timerJob: Job? = null
    private lateinit var repository: NamazRepository

    override fun onCreate() {
        super.onCreate()
        repository = NamazRepository(applicationContext)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                timerJob?.cancel()
                scope.launch {
                    repository.setActive(false, 0L)
                    NamazTileService.requestTileRefresh(applicationContext)
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
                return START_NOT_STICKY
            }
            ACTION_START, null -> {
                val notification = buildNotification()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
                startCountdownWatcher()
            }
        }
        return START_STICKY
    }

    private fun startCountdownWatcher() {
        timerJob?.cancel()
        timerJob = scope.launch {
            val snapshot = repository.getSnapshot()
            val endTime = if (snapshot.isNamazModeActive &&
                snapshot.endTimeMillis > System.currentTimeMillis()
            ) {
                snapshot.endTimeMillis
            } else {
                val newEnd = if (snapshot.autoTurnOff) {
                    System.currentTimeMillis() + snapshot.durationMinutes * 60_000L
                } else {
                    0L
                }
                repository.setActive(true, newEnd)
                NamazTileService.requestTileRefresh(applicationContext)
                newEnd
            }

            if (endTime > 0L) {
                while (true) {
                    val remaining = endTime - System.currentTimeMillis()
                    if (remaining <= 0L) {
                        repository.setActive(false, 0L)
                        NamazTileService.requestTileRefresh(applicationContext)
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                        break
                    }
                    delay(1000L)
                }
            }
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, NamazForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_namaz_tile)
            .setContentTitle(getString(R.string.notif_title_active))
            .setContentText(getString(R.string.notif_text_active))
            .setColor(0xFF00C853.toInt())
            .setOngoing(true)
            .setContentIntent(openPending)
            .addAction(0, getString(R.string.notif_action_turn_off), stopPending)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_status),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_status_desc)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        timerJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "namaz_mode_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.example.namaz.action.START"
        const val ACTION_STOP = "com.example.namaz.action.STOP"

        fun start(context: Context) {
            val intent = Intent(context, NamazForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, NamazForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
