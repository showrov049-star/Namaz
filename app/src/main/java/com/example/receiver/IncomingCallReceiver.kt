package com.example.receiver

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.example.data.CallEventInfo
import com.example.data.CallHandlingStrategy
import com.example.data.NamazRepository
import com.example.service.TtsVoiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IncomingCallReceiver : BroadcastReceiver() {

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        if (state != TelephonyManager.EXTRA_STATE_RINGING) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = NamazRepository(context)
                val settings = repo.getSnapshot()
                if (!settings.isNamazModeActive ||
                    !settings.autoCallResponseEnabled ||
                    settings.callStrategy != CallHandlingStrategy.AUTO_ANSWER_AND_TTS
                ) {
                    return@launch
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return@launch

                val hasAnswer = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ANSWER_PHONE_CALLS
                ) == PackageManager.PERMISSION_GRANTED
                if (!hasAnswer) return@launch

                val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    ?: "Unknown Number"
                val message = settings.resolveSpokenMessage()

                NamazRepository.publishCallEvent(
                    CallEventInfo(
                        callerNumber = number,
                        statusText = "Auto Answering...",
                        spokenMessage = message,
                        isActive = true
                    )
                )

                val telecom = context.getSystemService(TelecomManager::class.java)
                val audioManager = context.getSystemService(AudioManager::class.java)

                telecom?.acceptRingingCall()
                delay(1200L)

                audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
                audioManager?.isSpeakerphoneOn = true

                val tts = TtsVoiceManager(context)
                tts.speak(message, settings.voiceLanguage) {
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(500L)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ANSWER_PHONE_CALLS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            telecom?.endCall()
                        }
                        audioManager?.isSpeakerphoneOn = false
                        audioManager?.mode = AudioManager.MODE_NORMAL
                        tts.shutdown()
                        NamazRepository.clearActiveCallEvent()
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
