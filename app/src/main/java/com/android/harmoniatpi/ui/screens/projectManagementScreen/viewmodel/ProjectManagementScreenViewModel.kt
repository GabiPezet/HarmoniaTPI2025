package com.android.harmoniatpi.ui.screens.projectManagementScreen.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.cache.HoloJamCache
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
import com.android.harmoniatpi.domain.usecases.TrimAudioTrackUseCase
import com.android.harmoniatpi.domain.usecases.UndoTrimUseCase
import com.android.harmoniatpi.domain.usecases.UpdateOrInsertProjectInDBUseCase
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.ProyectScreenUiState
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
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
    private val trimAudioTrack: TrimAudioTrackUseCase,
    private val undoTrimUseCase: UndoTrimUseCase,
    private val getIfAllTracksWherePlayed: GetIfAllTracksWherePlayedUseCase,
    private val generateWaveform: GenerateWaveformUseCase,
    private val holoJamCache: HoloJamCache,
    private val updateOrInsertProjectInDBUseCase: UpdateOrInsertProjectInDBUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ProyectScreenUiState())
    private var selectedTrack: TrackUi? = null
    val state = _state.asStateFlow()

    init {
        _state.update {
            it.copy(currentProjectSelected = holoJamCache.currentProjectSelected)
        }
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
                        // update: guardamos el waveform
                        val result = generateWaveform(track.path)
                        _state.update { currentState ->
                            val updatedTracks = currentState.tracks.map { trackUi ->
                                if (trackUi.id == track.id) trackUi.copy(
                                    waveForm = result.waveform,
                                    durationMs = result.durationMs
                                ) else trackUi
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

    fun trimAudio(trackId: Long, startMs: Long, endMs: Long) {
        viewModelScope.launch {
            trimAudioTrack(trackId, startMs, endMs)
                .onSuccess {
                    Log.i(TAG, "Audio trim successful for track $trackId")
                    updateTrackUiAfterModification(trackId)
                }
                .onFailure {
                    Log.e(TAG, "Error trimming audio for track $trackId", it)
                }
        }
    }

    fun undoTrim(trackId: Long) {
        viewModelScope.launch {
            undoTrimUseCase(trackId)
                .onSuccess {
                    Log.i(TAG, "Undo trim successful for track $trackId")
                    updateTrackUiAfterModification(trackId)
                }
                .onFailure { e ->
                    Log.e(TAG, "Error undoing trim for track $trackId", e)
                    updateTrackUiAfterModification(trackId) // Forzar actualización para limpiar el estado de 'Undo' si falló la restauración/limpieza
                }
        }
    }

    private fun updateTrackUiAfterModification(trackId: Long) {
        val trackToUpdate = state.value.tracks.find { it.id == trackId }
        trackToUpdate?.let { trackUi ->

            val originalPath = trackUi.path.replace(".pcm", ".pcm.original")

            val result = generateWaveform(trackUi.path)
            val isUndoAvailable = File(originalPath).exists() // Determina el estado del botón Undo

            _state.update { currentState ->
                val updatedTracks = currentState.tracks.map { track ->
                    if (track.id == trackId) {
                        track.copy(
                            waveForm = result.waveform,
                            durationMs = result.durationMs,
                            isUndoAvailable = isUndoAvailable // <-- ESTADO DE UNDO
                        )
                    } else {
                        track
                    }
                }
                val timelineWidth = getUpdatedTimeline(updatedTracks)
                currentState.copy(
                    tracks = updatedTracks,
                    timelineWidth = timelineWidth
                )
            }
        }
    }

    fun previewTrim(trackId: Long, startMs: Long, endMs: Long) {
        viewModelScope.launch {
            // Detener cualquier reproducción en curso
            stopPlaying()

            val trackToPreview = getTracks().value.find { it.id == trackId }
            trackToPreview?.let { track ->

                track.setOnPlaybackCompletedCallback {
                    _state.update { it.copy(previewTrackId = null) }
                }

                track.playSegment(startMs, endMs)
                // Actualiza el estado para mostrar el icono de pausa
                _state.update { it.copy(isPlaying = false, previewTrackId = trackId) }
            }
        }
    }

    fun stopPreviewTrim(trackId: Long) {
        viewModelScope.launch {
            val trackToStop = getTracks().value.find { it.id == trackId }
            trackToStop?.stop()
            // Limpia el estado
            _state.update { it.copy(previewTrackId = null) }
        }
    }

    private fun fetchTracks() {
        viewModelScope.launch {
            getTracks().collect { domainTracks ->
                val currentUiTracks = _state.value.tracks
                _state.update { currentState ->
                    val updatedTracks = domainTracks.map { domainTrack ->
                        val path = domainTrack.path
                        val originalPath = path.replace(".pcm", ".pcm.original")
                        val isUndoAvailable =
                            File(originalPath).exists() // Comprueba estado al cargar

                        val result = generateWaveform(path)

                        // determina si es una pista existente o nueva
                        currentUiTracks.find { it.id == domainTrack.id }?.copy(
                            id = domainTrack.id,
                            path = domainTrack.path,
                            waveForm = result.waveform,
                            durationMs = result.durationMs,
                            isUndoAvailable = isUndoAvailable
                        ) ?: TrackUi(
                            title = "Voz",
                            selected = false,
                            id = domainTrack.id,
                            path = domainTrack.path,
                            waveForm = result.waveform,
                            durationMs = result.durationMs,
                            isUndoAvailable = isUndoAvailable
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
