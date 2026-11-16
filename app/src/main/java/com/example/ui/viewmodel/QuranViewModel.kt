package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DuaCategory
import com.example.data.IslamicContentData
import com.example.data.QuranAyah
import com.example.data.QuranRepository
import com.example.data.QuranSurahDetail
import com.example.data.QuranSurahSummary
import com.example.util.AudioPlaybackState
import com.example.util.QuranAudioPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuranUiState(
    val surahs: List<QuranSurahSummary> = IslamicContentData.offlineSurahs.map { it.summary },
    val isLoadingList: Boolean = false,
    val searchQuery: String = "",
    val selectedSurah: QuranSurahDetail? = null,
    val isLoadingSurah: Boolean = false,
    val surahError: String? = null,
    val selectedDuaCategory: DuaCategory = DuaCategory.ALL,
    val audioState: AudioPlaybackState = AudioPlaybackState()
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuranRepository()
    private val audioPlayer = QuranAudioPlayer()

    private val _uiState = MutableStateFlow(QuranUiState())
    val uiState: StateFlow<QuranUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            audioPlayer.state.collect { audio ->
                _uiState.update { it.copy(audioState = audio) }
            }
        }
        loadSurahList()
    }

    fun loadSurahList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingList = true) }
            val list = repository.getSurahList()
            _uiState.update { it.copy(surahs = list, isLoadingList = false) }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun openSurah(number: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSurah = true, surahError = null) }
            val result = repository.getSurahDetail(number)
            result.fold(
                onSuccess = { detail ->
                    _uiState.update {
                        it.copy(
                            selectedSurah = detail,
                            isLoadingSurah = false,
                            surahError = null
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingSurah = false,
                            surahError = err.message ?: "Unable to load Surah"
                        )
                    }
                }
            )
        }
    }

    fun closeSurah() {
        audioPlayer.stop()
        _uiState.update { it.copy(selectedSurah = null, surahError = null) }
    }

    fun selectDuaCategory(category: DuaCategory) {
        _uiState.update { it.copy(selectedDuaCategory = category) }
    }

    fun playOrToggleAyah(ayah: QuranAyah, autoAdvance: Boolean = true) {
        val key = "ayah_${ayah.globalNumber}"
        audioPlayer.playOrToggle(key, ayah.audioUrl) {
            if (autoAdvance) {
                val ayahs = _uiState.value.selectedSurah?.ayahs.orEmpty()
                val idx = ayahs.indexOfFirst { it.globalNumber == ayah.globalNumber }
                if (idx in 0 until ayahs.lastIndex) {
                    playOrToggleAyah(ayahs[idx + 1], autoAdvance = true)
                }
            }
        }
    }

    fun playSurahFromStart() {
        val ayahs = _uiState.value.selectedSurah?.ayahs.orEmpty()
        val first = ayahs.firstOrNull() ?: return
        val currentKey = _uiState.value.audioState.activeKey
        val activeAyah = ayahs.firstOrNull { "ayah_${it.globalNumber}" == currentKey }
        playOrToggleAyah(activeAyah ?: first, autoAdvance = true)
    }

    fun stopAudio() {
        audioPlayer.stop()
    }

    override fun onCleared() {
        audioPlayer.stop()
        super.onCleared()
    }
}
