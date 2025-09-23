package com.android.harmoniatpi.ui.screens.homeScreen.tabs.rehearsalRoomScreen.audiotests.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.android.harmoniatpi.domain.usecases.PauseAudioUseCase
import com.android.harmoniatpi.domain.usecases.PlayAudioUseCase
import com.android.harmoniatpi.domain.usecases.StartRecordingAudioUseCase
import com.android.harmoniatpi.domain.usecases.StopAudioUseCase
import com.android.harmoniatpi.domain.usecases.StopRecordingAudioUseCase
import com.android.harmoniatpi.ui.screens.homeScreen.tabs.rehearsalRoomScreen.audiotests.model.AudioTestUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AudioTestsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val startRecordingAudio: StartRecordingAudioUseCase,
    private val stopRecordingAudio: StopRecordingAudioUseCase,
    private val playAudio: PlayAudioUseCase,
    private val pauseAudio: PauseAudioUseCase,
    private val stopAudio: StopAudioUseCase,
) : ViewModel() {
    private val filePath = context.filesDir.absolutePath.plus("/test.pcm")
    private val _state = MutableStateFlow(AudioTestUiState())
    val state = _state.asStateFlow()


    fun startRecording() {
        val outputFile = File(filePath)
        startRecordingAudio(outputFile)
            .onSuccess {
                Log.i(TAG, "Grabación comenzada")
                _state.update {
                    it.copy(isRecording = true)
                }
            }
            .onFailure {
                Log.e(TAG, "Error al comenzar la grabación", it)
            }
    }

    fun stopRecording() {
        stopRecordingAudio()
        _state.update {
            it.copy(isRecording = false)
        }
    }

    fun play() {
        val inputFile = File(filePath)
        playAudio(inputFile)
            .onSuccess {
                Log.i(TAG, "Reproducción comenzada")
                _state.update {
                    it.copy(isPlaying = true)
                }
            }
            .onFailure {
                Log.e(TAG, "Error al comenzar la reproducción", it)
            }
    }

    fun pause() {
        pauseAudio()
        _state.update {
            it.copy(isPlaying = false)
        }
    }

    fun stopPlaying() {
        stopAudio()
        _state.update {
            it.copy(isPlaying = false)
        }
    }

    private companion object {
        const val TAG = "AudioTestsViewModel"
    }
}