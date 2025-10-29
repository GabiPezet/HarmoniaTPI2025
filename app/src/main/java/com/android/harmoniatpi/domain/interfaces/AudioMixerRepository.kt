package com.android.harmoniatpi.domain.interfaces

import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.domain.model.audio.Track
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Interfaz para operaciones de múltiples pistas de audio.
 */
interface AudioMixerRepository {
    /**
     * Reproduce las pistas.
     */
    fun play(excludeTrackId: Long? = null)

    /**
     * Pausa la reproducción de las pistas.
     */
    fun pause()

    /**
     * Para la reproducción de las pistas.
     */
    fun stop()

    /**
     * Crea una nueva pista.
     */
    fun createTrack(sourceType: AudioSourceType)

    /**
     * Crea una nueva pista a partir de un archivo de audio existente.
     * @param sourceFilePath Ruta del archivo de audio de origen.
     */
    suspend fun createTrackFromFile(sourceFilePath: String): Result<Unit>


    /**
     * Elimina una pista.
     * @param id Id de la pista a eliminar.
     */
    fun removeTrack(id: Long)

    /**
     * Recorta una pista de audio.
     * @param id Id de la pista a recortar.
     * @param startMs El tiempo de inicio del recorte en milisegundos.
     * @param endMs El tiempo de finalización del recorte en milisegundos.
     * @return Result<Unit> indicando el éxito o fallo de la operación.
     */
    fun trimTrack(id: Long, startMs: Long, endMs: Long): Result<Unit>

    /**
     * Deshace el último recorte realizado en una pista, restaurando el archivo original.
     * @param id Id de la pista.
     * @return Result<Unit> indicando el éxito o fallo de la operación.
     */
    fun undoTrim(id: Long): Result<Unit>

    /**
     * Obtiene las pistas actuales.
     */
    fun getTracks(): StateFlow<List<Track>>


    /**
     * Verifica si todas las pistas han sido reproducidas.
     */
    suspend fun allTracksWerePlayed(): StateFlow<Boolean>

    /**
     * Mutea una pista.
     * @param id Id de la pista a mutear.
     */
    fun muteTrack(id: Long)

    /**
     * Desmutea una pista.
     * @param id Id de la pista a desmutear.
     */
    fun unMuteTrack(id: Long)

    /**
     * Verifica si una pista está muteada.
     * @param id Id de la pista.
     */
    fun setTrackVolume(id: Long, volume: Float)

    /**
    * Establece un desplazamiento de inicio para la pista.
    * @param id Id de la pista.
    * @param offsetMs El tiempo de desplazamiento en milisegundos.
    */
    fun setTrackOffset(id: Long, offsetMs: Long)

    /**
     * Obtiene la posición de reproducción actual en milisegundos.
     */
    suspend fun getCurrentPlaybackPosition(): StateFlow<Long>

    /**
     * Se mueve a una posición específica en la reproducción.
     */
    fun seekTo(ms: Long)

    fun setPlaybackRange(trackId: Long, startMs: Long, endMs: Long, totalDurationMs: Long): Result<Unit>

    suspend fun loadPcmTrack(file: File, id: Long, sourceType: AudioSourceType, startOffsetMs: Long)

    fun clearAllTracks()

    fun playPreview(filePath: String): Result<Unit>

    fun stopPreview()

    fun onPreviewCompleted(): SharedFlow<Unit>

    fun cutAudioSegment(id: Long, startMs: Long, endMs: Long): Result<Unit>

    suspend fun addTrackFromSegment(sourcePath: String, startMs: Long, endMs: Long): Result<Unit>

    suspend fun applyDelayEffect(trackId: Long, delayTimeInSeconds: Float, decay: Float): Result<Unit>

    fun undoEffect(id: Long): Result<Unit>

}
