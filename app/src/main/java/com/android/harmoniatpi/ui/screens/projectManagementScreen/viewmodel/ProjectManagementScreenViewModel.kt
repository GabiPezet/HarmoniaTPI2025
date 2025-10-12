package com.android.harmoniatpi.ui.screens.projectManagementScreen.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.usecases.AddTrackUseCase
import com.android.harmoniatpi.domain.usecases.DeleteTrackUseCase
import com.android.harmoniatpi.domain.usecases.GenerateWaveformUseCase
import com.android.harmoniatpi.domain.usecases.GetIfAllTracksWherePlayedUseCase
import com.android.harmoniatpi.domain.usecases.GetTracksUseCase
import com.android.harmoniatpi.domain.usecases.PauseAudioUseCase
import com.android.harmoniatpi.domain.usecases.PlayAudioUseCase
import com.android.harmoniatpi.domain.usecases.StartRecordingAudioUseCase
import com.android.harmoniatpi.domain.usecases.StopAudioUseCase
import com.android.harmoniatpi.domain.usecases.StopRecordingAudioUseCase
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.ProyectScreenUiState
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectManagementScreenViewModel @Inject constructor(
    private val startRecordingAudio: StartRecordingAudioUseCase,
    private val stopRecordingAudio: StopRecordingAudioUseCase,
    private val playAudio: PlayAudioUseCase,
    private val pauseAudio: PauseAudioUseCase,
    private val stopAudio: StopAudioUseCase,
    private val getTracks: GetTracksUseCase,
    private val addTrack: AddTrackUseCase,
    private val deleteTrack: DeleteTrackUseCase,
    private val getIfAllTracksWherePlayed: GetIfAllTracksWherePlayedUseCase,
    private val generateWaveform: GenerateWaveformUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ProyectScreenUiState())
    private var selectedTrack: TrackUi? = null
    val state = _state.asStateFlow()

    init {
        fetchTracks()
        checkIfTracksWherePlayed()
    }

    fun startRecording() {
        selectedTrack = state.value.tracks.find { it.selected }
        selectedTrack?.let { track ->
            startRecordingAudio(track.path)
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
    }

    fun stopRecording() {
        stopRecordingAudio()
            .onSuccess {
                Log.i(TAG, "Grabación detenida")

                selectedTrack?.let { track ->
                    viewModelScope.launch {
                        val waveform = generateWaveform(track.path)
                        _state.update { currentState ->
                            val updatedTracks = currentState.tracks.map { trackUi ->
                                if (trackUi.id == track.id) trackUi.copy(waveForm = waveform) else trackUi
                            }

                            val timelineWidth = getUpdatedTimeline(updatedTracks)
                            currentState.copy(
                                tracks = updatedTracks,
                                timelineWidth = timelineWidth
                            )
                        }
                    }
                }
            }
            .onFailure {
                Log.e(TAG, "Error al detener la grabación", it)
            }
        _state.update {
            it.copy(isRecording = false)
        }
    }

    fun play() {
        _state.update {
            it.copy(isPlaying = true)
        }
        playAudio()
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

    fun addNewTrack() {
        addTrack()
    }

    fun deleteTrack() {
        selectedTrack?.let {
            deleteTrack(it.id)
        }
    }

    fun selectTrack(id: Long) {
        _state.update {
            it.copy(tracks = it.tracks.map { track ->
                if (track.id == id) {
                    selectedTrack = track
                    track.copy(selected = true)
                } else {
                    track.copy(selected = false)
                }
            })
        }
    }

    private fun fetchTracks() {
        viewModelScope.launch {
            getTracks().collect { domainTracks ->
                val currentUiTracks = _state.value.tracks
                _state.update { currentState ->
                    val updatedTracks = domainTracks.map { domainTrack ->
                        val existingUiTrack = currentUiTracks.find { it.id == domainTrack.id }
                        existingUiTrack?.copy(
                            id = domainTrack.id,
                            path = domainTrack.path
                        ) ?: TrackUi(
                            title = "Voz",
                            selected = false,
                            id = domainTrack.id,
                            path = domainTrack.path,
                        )
                    }
                    val timelineWidth = getUpdatedTimeline(updatedTracks)

                    currentState.copy(
                        tracks = updatedTracks,
                        timelineWidth = timelineWidth
                    )
                }
            }
        }
    }

    private fun checkIfTracksWherePlayed() {
        viewModelScope.launch {
            getIfAllTracksWherePlayed().collect { value ->
                if (value) {
                    _state.update {
                        it.copy(isPlaying = false)
                    }
                }
            }
        }
    }

    private fun getUpdatedTimeline(updatedTracks: List<TrackUi>): Int {
        if (updatedTracks.isEmpty()) return 500
        val maxWaveformSize = updatedTracks.maxOf { it.waveForm?.size ?: 0 }
        val timelineWidth = if (maxWaveformSize > 0) maxWaveformSize / 2 else 500
        return timelineWidth
    }

    private companion object {
        const val TAG = "AudioTestsViewModel"
    }
}
