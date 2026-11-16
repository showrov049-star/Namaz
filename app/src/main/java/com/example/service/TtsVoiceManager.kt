package com.example.service

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.VoiceLanguage
import java.util.Locale
import java.util.UUID

class TtsVoiceManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeak: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            isInitialized = (status == TextToSpeech.SUCCESS)
            if (isInitialized) {
                pendingSpeak?.invoke()
                pendingSpeak = null
            }
        }
    }

    fun speak(
        text: String,
        language: VoiceLanguage,
        onDone: () -> Unit = {}
    ) {
        val action: () -> Unit = {
            val engine = tts
            if (engine != null && isInitialized) {
                val targetLocale = when (language) {
                    VoiceLanguage.BENGALI -> Locale.forLanguageTag("bn-BD")
                    VoiceLanguage.ENGLISH -> Locale.US
                }
                val result = engine.setLanguage(targetLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    if (language == VoiceLanguage.BENGALI) {
                        val fallbackBn = engine.setLanguage(Locale.forLanguageTag("bn-IN"))
                        if (fallbackBn == TextToSpeech.LANG_MISSING_DATA ||
                            fallbackBn == TextToSpeech.LANG_NOT_SUPPORTED
                        ) {
                            engine.language = Locale.US
                        }
                    } else {
                        engine.language = Locale.US
                    }
                }
                engine.setSpeechRate(0.95f)
                val utteranceId = UUID.randomUUID().toString()
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) {}
                    override fun onDone(id: String?) {
                        if (id == utteranceId) onDone()
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(id: String?) {
                        if (id == utteranceId) onDone()
                    }
                })
                engine.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), utteranceId)
            } else {
                onDone()
            }
        }

        if (isInitialized) action() else pendingSpeak = action
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
