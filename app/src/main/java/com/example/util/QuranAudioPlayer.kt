package com.example.util

import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AudioPlaybackState(
    val activeKey: String? = null,
    val isBuffering: Boolean = false,
    val isPlaying: Boolean = false,
    val errorMessage: String? = null
)

class QuranAudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var completionCallback: (() -> Unit)? = null

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    fun playOrToggle(key: String, url: String, onCompleted: (() -> Unit)? = null) {
        val current = _state.value
        if (current.activeKey == key && mediaPlayer != null) {
            val mp = mediaPlayer!!
            if (current.isPlaying) {
                runCatching { mp.pause() }
                _state.update { it.copy(isPlaying = false, isBuffering = false) }
            } else if (!current.isBuffering) {
                runCatching { mp.start() }
                _state.update { it.copy(isPlaying = true) }
            }
            return
        }

        stop()
        completionCallback = onCompleted
        _state.value = AudioPlaybackState(
            activeKey = key,
            isBuffering = true,
            isPlaying = false,
            errorMessage = null
        )

        val mp = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            setOnPreparedListener { player ->
                player.start()
                _state.update { it.copy(isBuffering = false, isPlaying = true, errorMessage = null) }
            }
            setOnCompletionListener {
                _state.update { it.copy(isPlaying = false, isBuffering = false) }
                completionCallback?.invoke()
            }
            setOnErrorListener { _, _, _ ->
                _state.value = AudioPlaybackState(
                    activeKey = null,
                    isBuffering = false,
                    isPlaying = false,
                    errorMessage = "Audio stream unavailable offline."
                )
                true
            }
        }
        mediaPlayer = mp

        runCatching {
            mp.setDataSource(url)
            mp.prepareAsync()
        }.onFailure { err ->
            stop()
            _state.value = AudioPlaybackState(errorMessage = err.message ?: "Unable to play audio")
        }
    }

    fun stop() {
        completionCallback = null
        mediaPlayer?.let { mp ->
            runCatching {
                if (mp.isPlaying) mp.stop()
                mp.reset()
                mp.release()
            }
        }
        mediaPlayer = null
        _state.value = AudioPlaybackState()
    }
}
