package com.android.harmoniatpi.ui.screens.projectManagementScreen.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.cache.HoloJamCache
import com.android.harmoniatpi.domain.usecases.AddTrackFromFileUseCase
import com.android.harmoniatpi.domain.usecases.AddTrackUseCase
import com.android.harmoniatpi.domain.usecases.DeleteTrackUseCase
import com.android.harmoniatpi.domain.usecases.GenerateWaveformUseCase
import com.android.harmoniatpi.domain.usecases.GetIfAllTracksWherePlayedUseCase
import com.android.harmoniatpi.domain.usecases.GetTracksUseCase
import com.android.harmoniatpi.domain.usecases.LoadProjectTrackUseCase
import com.android.harmoniatpi.domain.usecases.MuteTrackUseCase
import com.android.harmoniatpi.domain.usecases.PauseAudioUseCase
import com.android.harmoniatpi.domain.usecases.PlayAudioUseCase
import com.android.harmoniatpi.domain.usecases.SetTrackVolumeUseCase
import com.android.harmoniatpi.domain.usecases.StartRecordingAudioUseCase
import com.android.harmoniatpi.domain.usecases.StopAudioUseCase
import com.android.harmoniatpi.domain.usecases.StopRecordingAudioUseCase
import com.android.harmoniatpi.domain.usecases.TrimAudioTrackUseCase
import com.android.harmoniatpi.domain.usecases.UnMuteTrackUseCase
import com.android.harmoniatpi.domain.usecases.UndoTrimUseCase
import com.android.harmoniatpi.domain.usecases.UpdateOrInsertProjectInDBUseCase
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.ProyectScreenUiState
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@HiltViewModel
class ProjectManagementScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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
    private val addTrackFromFileUseCase: AddTrackFromFileUseCase,
    private val holoJamCache: HoloJamCache,
    private val updateOrInsertProjectInDBUseCase: UpdateOrInsertProjectInDBUseCase,
    private val muteTrackUseCase: MuteTrackUseCase,
    private val unMuteTrackUseCase: UnMuteTrackUseCase,
    private val setTrackVolumeUseCase: SetTrackVolumeUseCase,
    private val loadProjectTrackUseCase: LoadProjectTrackUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ProyectScreenUiState())
    private var selectedTrack: TrackUi? = null
    val state = _state.asStateFlow()

    init {
        val project = holoJamCache.currentProjectSelected
        _state.update {
            it.copy(currentProjectSelected = project)
        }.apply {
            Log.i("KlyxDevs", "currentProjectSelected: ${state.value.currentProjectSelected}")
        }

        viewModelScope.launch {
            fetchTracks()
            if (!project?.urlAudioTracks.isNullOrEmpty()) {
                Log.i("KlyxDevs", " Restaurando pistas del proyecto guardado...")

                loadProjectTrackUseCase.clearAllTracks()

                val loadedTracks = project.urlAudioTracks.map { it.toTrackUi() }
                _state.update { currentState ->
                    currentState.copy(tracks = loadedTracks)
                }

                // Reinyectar las pistas en el motor de audio (AudioMixerRepository)
                project.urlAudioTracks.forEach { audioTrack ->
                    val path = audioTrack.path
                    val file = File(path)

                    if (file.exists()) {
                        Log.i("KlyxDevs", " Archivo encontrado en $path — restaurando pista...")

                        loadProjectTrackUseCase(path, audioTrack.id)
                            .onSuccess {
                                Log.i(
                                    "KlyxDevs",
                                    " Pista restaurada correctamente: ${audioTrack.id}"
                                )
                            }
                            .onFailure { e ->
                                Log.e(
                                    "KlyxDevs",
                                    " Error restaurando pista ${audioTrack.id}: ${e.message}"
                                )
                            }
                    } else {
                        Log.w(
                            "KlyxDevs",
                            " Archivo no encontrado para pista ${audioTrack.id}: $path"
                        )
                    }
                }


                delay(500)
                _state.update { current ->
                    val updatedTracks = current.tracks.map { trackUi ->
                        val result = generateWaveform(trackUi.path)
                        trackUi.copy(
                            waveForm = result.waveform,
                            durationMs = result.durationMs
                        )
                    }
                    val timelineWidth = getUpdatedTimeline(updatedTracks)
                    current.copy(tracks = updatedTracks, timelineWidth = timelineWidth)
                }

                Log.i("KlyxDevs", " Todas las pistas fueron restauradas y listas para reproducir.")
            } else {
                Log.i("KlyxDevs", " Proyecto sin pistas previas — iniciando flujo normal.")

            }

            checkIfTracksWherePlayed()
        }

    }

    fun updateCurrentProjectWithTracks() {
        val currentState = _state.value
        val project = currentState.currentProjectSelected ?: return
        currentState.tracks.forEach {
            Log.i("KlyxDevs", "updateCurrentProjectWithTracks TRACKS: ${it.path}")
        }
        val updatedProject = project.copy(
            urlAudioTracks = currentState.tracks.map { it.toAudioTrack() }
        )

        _state.update {
            it.copy(currentProjectSelected = updatedProject)
        }

        // Guardar en cache y/o base de datos
        viewModelScope.launch {
            updateOrInsertProjectInDBUseCase(updatedProject)
            updatedProject.urlAudioTracks.forEach {
                Log.i("KlyxDevs", "updateCurrentProjectWithTracks AUDIOTRACKS: ${it.path}")
            }
        }
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
                    Log.i("KlyxDevs", "startRecordingAudio: ${track.path}")
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
        }.apply {
            _state.update {
                it.copy(tracks = it.tracks.filter { track -> track.id != selectedTrack?.id })
            }
            viewModelScope.launch {
                updateCurrentProjectWithTracks()
            }
        }
    }

    fun importTrackFromFile(uri: Uri) {
        viewModelScope.launch {


            val tempFile = File(context.cacheDir, "temp_import_${System.currentTimeMillis()}.tmp")

            try {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(tempFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }

                addTrackFromFileUseCase(tempFile.absolutePath)
                    .onSuccess {
                        Log.i(TAG, "Pista importada y convertida exitosamente desde $uri")
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Error importando pista desde $uri: ${e.message}", e)
                        // TODO: Mostrar Toast con error
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error resolviendo o copiando archivo de origen: ${e.message}", e)
                // TODO: Mostrar Toast con error
            } finally {
                tempFile.delete()
            }
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
                    updateTrackUiAfterModification(trackId) // Forzar actualizacion si falló
                }
        }
    }

    private fun updateTrackUiAfterModification(trackId: Long) {
        val trackToUpdate = state.value.tracks.find { it.id == trackId }
        trackToUpdate?.let { trackUi ->

            val originalPath = trackUi.path.replace(".pcm", ".pcm.original")

            val result = generateWaveform(trackUi.path)
            val isUndoAvailable = File(originalPath).exists()

            _state.update { currentState ->
                val updatedTracks = currentState.tracks.map { track ->
                    if (track.id == trackId) {
                        track.copy(
                            waveForm = result.waveform,
                            durationMs = result.durationMs,
                            isUndoAvailable = isUndoAvailable
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
            // Detener cualquier reproducción
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

    fun muteTrack() {
        selectedTrack?.let {
            muteTrackUseCase(it.id)
            updateTrackMuteState(it.id, true)
        }
    }

    fun unMuteTrack() {
        selectedTrack?.let {
            unMuteTrackUseCase(it.id)
            updateTrackMuteState(it.id, false)
        }
    }

    fun setTrackVolume(volume: Float) {
        selectedTrack?.let {
            setTrackVolumeUseCase(it.id, volume)
            updateTrackVolume(it.id, volume)
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
                        val isUndoAvailable = File(originalPath).exists()

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

    private fun updateTrackMuteState(trackId: Long, isMuted: Boolean) {
        _state.update { currentState ->
            val updatedTracks = currentState.tracks.map { track ->
                if (track.id == trackId) track.copy(isMuted = isMuted) else track
            }
            currentState.copy(tracks = updatedTracks)
        }
    }

    private fun updateTrackVolume(trackId: Long, volume: Float) {
        _state.update { currentState ->
            val updatedTracks = currentState.tracks.map { track ->
                if (track.id == trackId) track.copy(volume = volume) else track
            }
            currentState.copy(tracks = updatedTracks)
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
