package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.CallEventInfo
import com.example.data.CallHandlingStrategy
import com.example.data.NamazRepository
import kotlinx.coroutines.runBlocking

class NamazCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            callDetails.callDirection != Call.Details.DIRECTION_INCOMING
        ) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val repo = NamazRepository(applicationContext)
        val settings = runBlocking { repo.getSnapshot() }

        if (!settings.isNamazModeActive || !settings.autoCallResponseEnabled) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val number = callDetails.handle?.schemeSpecificPart ?: "Unknown Number"
        val message = settings.resolveSpokenMessage()

        if (settings.callStrategy == CallHandlingStrategy.SCREEN_AND_REJECT) {
            val builder = CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(false)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setSilenceCall(true)
            }
            respondToCall(callDetails, builder.build())

            NamazRepository.publishCallEvent(
                CallEventInfo(
                    callerNumber = number,
                    statusText = "Call Screened & Silenced during Salah",
                    spokenMessage = message,
                    isActive = true
                )
            )
            postBlockedCallNotification(number)
        } else {
            respondToCall(callDetails, CallResponse.Builder().build())
        }
    }

    private fun postBlockedCallNotification(caller: String) {
        val nm = getSystemService(NotificationManager::class.java) ?: return
        val channelId = "namaz_call_alerts"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    getString(R.string.notif_channel_calls),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_namaz_tile)
            .setContentTitle(getString(R.string.notif_call_handled_title))
            .setContentText("Incoming call from $caller was silenced/rejected.")
            .setAutoCancel(true)
            .build()
        nm.notify(caller.hashCode(), notification)
    }
}
