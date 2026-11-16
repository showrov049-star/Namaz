package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.R
import com.example.data.HapticIntensity

enum class FeedbackAction {
    HOME_CLICK,
    NAMAZ_ON,
    NAMAZ_OFF,
    SETTINGS_CLICK,
    TOGGLE_SWITCH,
    MOSQUE_FINDER,
    MARKER_POP,
    NAVIGATION_CONFIRM,
    SAVE_SUCCESS,
    BACK_TAP,
    KEYBOARD_TAP
}

class FeedbackManager(context: Context) {

    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val soundPool: SoundPool
    private val soundIds = mutableMapOf<FeedbackAction, Int>()
    private val loadedSounds = mutableSetOf<Int>()

    @Volatile
    var touchSoundsEnabled: Boolean = true
    @Volatile
    var hapticFeedbackEnabled: Boolean = true
    @Volatile
    var soundVolume: Float = 0.75f
    @Volatile
    var hapticIntensity: HapticIntensity = HapticIntensity.MEDIUM

    init {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
            }
        }

        soundIds[FeedbackAction.HOME_CLICK] = soundPool.load(appContext, R.raw.sfx_home_click, 1)
        soundIds[FeedbackAction.NAMAZ_ON] = soundPool.load(appContext, R.raw.sfx_namaz_on, 1)
        soundIds[FeedbackAction.NAMAZ_OFF] = soundPool.load(appContext, R.raw.sfx_namaz_off, 1)
        soundIds[FeedbackAction.SETTINGS_CLICK] = soundPool.load(appContext, R.raw.sfx_settings_click, 1)
        soundIds[FeedbackAction.TOGGLE_SWITCH] = soundPool.load(appContext, R.raw.sfx_toggle_switch, 1)
        soundIds[FeedbackAction.MOSQUE_FINDER] = soundPool.load(appContext, R.raw.sfx_mosque_finder, 1)
        soundIds[FeedbackAction.MARKER_POP] = soundPool.load(appContext, R.raw.sfx_marker_pop, 1)
        soundIds[FeedbackAction.NAVIGATION_CONFIRM] = soundPool.load(appContext, R.raw.sfx_navigation_confirm, 1)
        soundIds[FeedbackAction.SAVE_SUCCESS] = soundPool.load(appContext, R.raw.sfx_save_success, 1)
        soundIds[FeedbackAction.BACK_TAP] = soundPool.load(appContext, R.raw.sfx_back_tap, 1)
    }

    fun updatePreferences(
        soundsEnabled: Boolean,
        hapticsEnabled: Boolean,
        volume: Float,
        intensity: HapticIntensity
    ) {
        touchSoundsEnabled = soundsEnabled
        hapticFeedbackEnabled = hapticsEnabled
        soundVolume = volume.coerceIn(0f, 1f)
        hapticIntensity = intensity
    }

    fun play(
        action: FeedbackAction,
        overrideSoundEnabled: Boolean = touchSoundsEnabled,
        overrideHapticEnabled: Boolean = hapticFeedbackEnabled,
        overrideVolume: Float = soundVolume,
        overrideIntensity: HapticIntensity = hapticIntensity,
        ignoreRingerModeForPreview: Boolean = false
    ) {
        if (overrideSoundEnabled) {
            playSoundInternal(action, overrideVolume.coerceIn(0f, 1f), ignoreRingerModeForPreview)
        }
        if (overrideHapticEnabled) {
            triggerHapticInternal(action, overrideIntensity)
        }
    }

    private fun playSoundInternal(
        action: FeedbackAction,
        volume: Float,
        ignoreRingerMode: Boolean
    ) {
        if (volume <= 0.01f) return
        if (!ignoreRingerMode && audioManager?.ringerMode == AudioManager.RINGER_MODE_SILENT) {
            return
        }
        val mappedAction = if (action == FeedbackAction.KEYBOARD_TAP) {
            FeedbackAction.BACK_TAP
        } else {
            action
        }
        val sampleId = soundIds[mappedAction] ?: return
        if (!loadedSounds.contains(sampleId)) return

        val finalVol = if (action == FeedbackAction.KEYBOARD_TAP) volume * 0.45f else volume
        soundPool.play(sampleId, finalVol, finalVol, 1, 0, 1.0f)
    }

    private fun triggerHapticInternal(action: FeedbackAction, intensity: HapticIntensity) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return
        if (!isSystemHapticEnabled()) return

        val scale = intensity.scale
        val supportsAmplitude = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vib.hasAmplitudeControl()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (action) {
                FeedbackAction.KEYBOARD_TAP -> {
                    val amp = (95 * scale).toInt().coerceIn(20, 255)
                    if (supportsAmplitude) {
                        VibrationEffect.createOneShot(9L, amp)
                    } else {
                        VibrationEffect.createOneShot(9L, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }
                FeedbackAction.HOME_CLICK,
                FeedbackAction.SETTINGS_CLICK,
                FeedbackAction.BACK_TAP -> {
                    val amp = (120 * scale).toInt().coerceIn(30, 255)
                    if (supportsAmplitude) {
                        VibrationEffect.createOneShot(14L, amp)
                    } else {
                        VibrationEffect.createOneShot(14L, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }
                FeedbackAction.TOGGLE_SWITCH,
                FeedbackAction.MOSQUE_FINDER -> {
                    val amp = (155 * scale).toInt().coerceIn(40, 255)
                    if (supportsAmplitude) {
                        VibrationEffect.createOneShot(18L, amp)
                    } else {
                        VibrationEffect.createOneShot(18L, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }
                FeedbackAction.MARKER_POP -> {
                    val amp = (150 * scale).toInt().coerceIn(40, 255)
                    if (supportsAmplitude) {
                        VibrationEffect.createOneShot(20L, amp)
                    } else {
                        VibrationEffect.createOneShot(20L, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                }
                FeedbackAction.NAMAZ_ON,
                FeedbackAction.NAMAZ_OFF,
                FeedbackAction.NAVIGATION_CONFIRM -> {
                    val lowAmp = (130 * scale).toInt().coerceIn(40, 255)
                    val highAmp = (225 * scale).toInt().coerceIn(60, 255)
                    if (supportsAmplitude) {
                        VibrationEffect.createWaveform(
                            longArrayOf(0L, 22L, 38L, 30L),
                            intArrayOf(0, lowAmp, 0, highAmp),
                            -1
                        )
                    } else {
                        VibrationEffect.createWaveform(longArrayOf(0L, 22L, 38L, 30L), -1)
                    }
                }
                FeedbackAction.SAVE_SUCCESS -> {
                    val midAmp = (145 * scale).toInt().coerceIn(45, 255)
                    val highAmp = (240 * scale).toInt().coerceIn(70, 255)
                    if (supportsAmplitude) {
                        VibrationEffect.createWaveform(
                            longArrayOf(0L, 18L, 42L, 34L),
                            intArrayOf(0, midAmp, 0, highAmp),
                            -1
                        )
                    } else {
                        VibrationEffect.createWaveform(longArrayOf(0L, 18L, 42L, 34L), -1)
                    }
                }
            }
            vib.vibrate(effect)
        } else {
            val ms = when (action) {
                FeedbackAction.KEYBOARD_TAP -> 8L
                FeedbackAction.HOME_CLICK, FeedbackAction.SETTINGS_CLICK, FeedbackAction.BACK_TAP -> 12L
                FeedbackAction.TOGGLE_SWITCH, FeedbackAction.MOSQUE_FINDER, FeedbackAction.MARKER_POP -> 18L
                FeedbackAction.NAMAZ_ON, FeedbackAction.NAMAZ_OFF,
                FeedbackAction.NAVIGATION_CONFIRM, FeedbackAction.SAVE_SUCCESS -> 32L
            }
            @Suppress("DEPRECATION")
            vib.vibrate(ms)
        }
    }

    @Suppress("DEPRECATION")
    private fun isSystemHapticEnabled(): Boolean {
        return runCatching {
            Settings.System.getInt(
                appContext.contentResolver,
                Settings.System.HAPTIC_FEEDBACK_ENABLED,
                1
            ) != 0
        }.getOrDefault(true)
    }

    fun release() {
        soundPool.release()
    }
}

val LocalFeedbackManager = staticCompositionLocalOf<FeedbackManager?> { null }
