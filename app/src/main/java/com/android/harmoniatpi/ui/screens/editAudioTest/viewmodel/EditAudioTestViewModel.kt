package com.android.harmoniatpi.ui.screens.editAudioTest.viewmodel

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.ui.screens.editAudioTest.model.EditAudioTestState
import com.android.harmoniatpi.ui.screens.editAudioTest.model.RecordMode
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditAudioTestViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditAudioTestState())
    val uiState = _uiState.asStateFlow()

    private val audioRecorder = AudioRecorder(context)
    private var mediaPlayer: MediaPlayer? = null
    private var playbackTimer: Job? = null

    fun setMode(mode: RecordMode) {
        _uiState.update { it.copy(recordMode = mode) }
    }

    fun startAudioRecording() {
        viewModelScope.launch {
            val uri = audioRecorder.startRecording()
            if (uri != null) {
                _uiState.update { it.copy(isRecording = true, recordingDuration = 0) }
                val start = System.currentTimeMillis()
                while (_uiState.value.isRecording) {
                    delay(1000)
                    val dur = ((System.currentTimeMillis() - start) / 1000)
                    _uiState.update { it.copy(recordingDuration = dur) }
                }
            }
        }
    }

    fun stopAudioRecording() {
        val uri = audioRecorder.stopRecording()
        _uiState.update {
            it.copy(
                isRecording = false,
                recordedFileUri = uri,
                recordingDuration = 0
            )
        }
    }

    fun togglePlayback() {
        if (_uiState.value.isPlaying) stopPlayback() else startPlayback()
    }

    private fun startPlayback() {
        val uri = _uiState.value.recordedFileUri ?: return
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setOnCompletionListener { stopPlayback() }
                prepare()
                start()
            }
            _uiState.update { it.copy(isPlaying = true) }
            startPlaybackProgressUpdates()
        } catch (e: Exception) {
            Log.e("EditAudioTestVM", "Error playback", e)
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        playbackTimer?.cancel()
        _uiState.update { it.copy(isPlaying = false, playbackProgress = 0f) }
    }

    private fun startPlaybackProgressUpdates() {
        playbackTimer?.cancel()
        playbackTimer = viewModelScope.launch {
            while (_uiState.value.isPlaying) {
                mediaPlayer?.let { p ->
                    if (p.isPlaying) {
                        val progress = p.currentPosition.toFloat() / p.duration.toFloat()
                        _uiState.update { it.copy(playbackProgress = progress) }
                    }
                }
                delay(100)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.stopRecording()
        mediaPlayer?.release()
        playbackTimer?.cancel()
    }
}
