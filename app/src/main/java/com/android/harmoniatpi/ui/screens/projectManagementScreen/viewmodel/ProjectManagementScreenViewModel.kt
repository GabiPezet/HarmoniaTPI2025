package com.android.harmoniatpi.ui.screens.projectManagementScreen.viewmodel

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.harmoniatpi.domain.cache.HoloJamCache
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.domain.usecases.audioUseCases.AddTrackFromFileUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.AddTrackUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.ApplyDelayEffectUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.DeleteTrackUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.GenerateWaveformUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.GetCurrentPlaybackPositionUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.GetIfAllTracksWherePlayedUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.GetTracksUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.LoadProjectTrackUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.MuteTrackUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.PauseAudioUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.PlayAudioUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.SeekToUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.SetTrackOffsetUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.SetTrackVolumeUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.StartRecordingAudioUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.StopAudioUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.StopRecordingAudioUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.TrimAudioTrackUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.UnMuteTrackUseCase
import com.android.harmoniatpi.domain.usecases.audioUseCases.UndoTrimUseCase
import com.android.harmoniatpi.domain.usecases.roomUseCases.UpdateOrInsertProjectInDBUseCase
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.ProyectScreenUiState
import com.android.harmoniatpi.ui.screens.projectManagementScreen.model.TrackUi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

private const val MS_PER_DP_SCALE = 10f

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
    private val getCurrentPlaybackPosition: GetCurrentPlaybackPositionUseCase,
    private val seekToUseCase: SeekToUseCase,
    private val loadProjectTrackUseCase: LoadProjectTrackUseCase,
    private val setTrackOffsetUseCase: SetTrackOffsetUseCase,
    private val applyDelayEffectUseCase: ApplyDelayEffectUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ProyectScreenUiState())
    private var selectedTrack: TrackUi? = null
    val state = _state.asStateFlow()
    private val originalVolumes = mutableMapOf<Long, Float>()


    init {
        startPlaybackObserver()
        fetchTracks()
        checkIfTracksWherePlayed()
        loadProjectFromCache()
    }

    private fun loadProjectFromCache() {
        val project = holoJamCache.currentProjectSelected
        _state.update { it.copy(currentProjectSelected = project) }

        if (project != null && project.urlAudioTracks.isNotEmpty()) {
            viewModelScope.launch {
                Log.i("KlyxDevs", "Restaurando pistas del proyecto guardado...")
                loadProjectTrackUseCase.clearAllTracks()

                project.urlAudioTracks.forEach { audioTrack ->
                    val file = File(audioTrack.path)
                    if (file.exists()) {
                        loadProjectTrackUseCase(audioTrack.path, audioTrack.id, audioTrack.sourceType)
                            .onSuccess { Log.i("KlyxDevs", "Pista restaurada en el mixer: ${audioTrack.id}") }
                            .onFailure { e -> Log.e("KlyxDevs", "Error restaurando pista ${audioTrack.id}: ${e.message}") }
                    } else {
                        Log.w("KlyxDevs", "Archivo no encontrado para pista ${audioTrack.id}: ${audioTrack.path}")
                    }
                }
            }
        } else {
            Log.i("KlyxDevs", "Proyecto nuevo o sin pistas guardadas.")
        }
    }


    private fun getUpdatedTimeline(updatedTracks: List<TrackUi>): Int {
        if (updatedTracks.isEmpty()) return 500 // Valor base


        val maxDurationPlusOffset = updatedTracks.maxOf {
            (it.durationMs + it.startOffsetMs).coerceAtLeast(0L)
        }

        val timelineWidthInDp = (maxDurationPlusOffset / MS_PER_DP_SCALE).toInt()

        return timelineWidthInDp.coerceAtLeast(500)
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
        selectedTrack?.let { trackToRecord ->

            originalVolumes.clear()
            val tracksToPlay = state.value.tracks.filter { it.id != trackToRecord.id }

            tracksToPlay.forEach { track ->
                originalVolumes[track.id] = track.volume
                setTrackVolumeUseCase(track.id, 0.03f)
            }


            val audioSource = if (trackToRecord.sourceType == AudioSourceType.VOICE) {
                MediaRecorder.AudioSource.VOICE_COMMUNICATION
            } else {
                MediaRecorder.AudioSource.MIC
            }

            playAudio(excludeTrackId = trackToRecord.id)
            _state.update { it.copy(isPlaying = true) }

            startRecordingAudio(trackToRecord.path, audioSource)
                .onSuccess {
                    _state.update { it.copy(isRecording = true) }
                }
                .onFailure {
                    stopPlaying()
                    restoreOriginalVolumes()
                }
        }
    }

    fun stopRecording() {
        stopRecordingAudio()
            .onSuccess {
                Log.i(TAG, "Grabación detenida")
                stopPlaying()
                restoreOriginalVolumes()

                selectedTrack?.let { recordedTrack ->
                    viewModelScope.launch {
                        val result = generateWaveform(recordedTrack.path)
                        _state.update { currentState ->
                            val updatedTracks = currentState.tracks.map { trackUi ->
                                if (trackUi.id == recordedTrack.id) {
                                    trackUi.copy(
                                        waveForm = result.waveform,
                                        durationMs = result.durationMs
                                    )
                                } else {
                                    trackUi
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
            }
            .onFailure {
                Log.e(TAG, "Error al detener la grabación", it)
                restoreOriginalVolumes()
            }
        _state.update { it.copy(isRecording = false) }
    }

    private fun restoreOriginalVolumes() {
        originalVolumes.forEach { (trackId, volume) ->
            setTrackVolumeUseCase(trackId, volume)
        }
        originalVolumes.clear()
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

    fun addNewTrack(sourceType: AudioSourceType) {
        addTrack(sourceType)
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
                        Toast.makeText(context, "Pista importada exitosamente", Toast.LENGTH_SHORT).show()
                    }
                    .onFailure { e ->
                        Log.e(TAG, "Error importando pista desde $uri: ${e.message}", e)
                        Toast.makeText(context, "Error al importar la pista", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error resolviendo o copiando archivo de origen: ${e.message}", e)
                Toast.makeText(context, "Error al procesar el archivo", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Pista recortada", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Audio trim successful for track $trackId")
                    updateTrackUiAfterModification(trackId)
                }
                .onFailure {
                    Log.e(TAG, "Error trimming audio for track $trackId", it)
                    Toast.makeText(context, "Error al recortar la pista", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun undoTrim(trackId: Long) {
        viewModelScope.launch {
            undoTrimUseCase(trackId)
                .onSuccess {
                    Log.i(TAG, "Undo trim successful for track $trackId")
                    updateTrackUiAfterModification(trackId)
                    Toast.makeText(context, "Recorte deshecho", Toast.LENGTH_SHORT).show()
                }
                .onFailure { e ->
                    Log.e(TAG, "Error undoing trim for track $trackId", e)
                    Toast.makeText(context, "No se pudo deshacer el recorte", Toast.LENGTH_SHORT).show()
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

    fun updateTrackOffset(trackId: Long, offsetMs: Long) {
        setTrackOffsetUseCase(trackId, offsetMs)

        _state.update { currentState ->
            val updatedTracks = currentState.tracks.map { track ->
                if (track.id == trackId) {
                    track.copy(startOffsetMs = offsetMs)
                } else {
                    track
                }
            }
            // debo recalcular ancho de la timeline
            val timelineWidth = getUpdatedTimeline(updatedTracks)

            currentState.copy(
                tracks = updatedTracks,
                timelineWidth = timelineWidth
            )
        }
    }

    private fun startPlaybackObserver() {
        viewModelScope.launch {
            getCurrentPlaybackPosition().collect { ms ->
                _state.update { it.copy(currentPlaybackMs = ms) }
            }
        }
    }

    fun seekAndPlay(ms: Long) {
        seekToUseCase(ms)
    }

    fun clearAllTracks() {
        loadProjectTrackUseCase.clearAllTracks()
    }


    private fun fetchTracks() {
        viewModelScope.launch {
            getTracks().collect { domainTracks ->
                val savedTracks = _state.value.currentProjectSelected?.urlAudioTracks ?: emptyList()

                val updatedTracks = domainTracks.map { domainTrack ->
                    val savedTrack = savedTracks.find { it.id == domainTrack.id }
                    val title = if (domainTrack.sourceType == AudioSourceType.VOICE) "Voz" else "Instrumento"

                    TrackUi(
                        id = domainTrack.id,
                        path = domainTrack.path,
                        title = savedTrack?.title ?: title,
                        selected = state.value.tracks.find { it.id == domainTrack.id }?.selected ?: false,
                        sourceType = domainTrack.sourceType,
                        waveForm = savedTrack?.waveForm ?: generateWaveform(domainTrack.path).waveform,
                        durationMs = savedTrack?.durationMs ?: generateWaveform(domainTrack.path).durationMs,
                        isUndoAvailable = File(domainTrack.originalPath).exists(),
                        startOffsetMs = savedTrack?.startOffsetMs ?: domainTrack.startOffsetMs,
                        isMuted = savedTrack?.isMuted ?: domainTrack.isMuted(),
                        volume = savedTrack?.volume ?: domainTrack.getVolume()
                    )
                }

                val timelineWidth = getUpdatedTimeline(updatedTracks)
                _state.update { it.copy(tracks = updatedTracks, timelineWidth = timelineWidth) }
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


    fun applyDelayEffect(trackId: Long, delayTimeInSeconds: Float, decay: Float) {
        viewModelScope.launch {
            applyDelayEffectUseCase(trackId, delayTimeInSeconds, decay)
                .onSuccess {
                    Log.i(TAG, "Efecto de delay aplicado a $trackId")
                    Toast.makeText(context, "Efecto aplicado", Toast.LENGTH_SHORT).show()

                    updateTrackUiAfterModification(trackId)
                }
                .onFailure { e ->
                    Log.e(TAG, "Error aplicando delay", e)
                    Toast.makeText(context, "Error al aplicar efecto", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private companion object {
        const val TAG = "AudioTestsViewModel"
    }

}
