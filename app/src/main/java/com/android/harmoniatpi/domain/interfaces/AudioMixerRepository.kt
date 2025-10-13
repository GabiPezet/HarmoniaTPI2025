package com.android.harmoniatpi.domain.interfaces

import com.android.harmoniatpi.domain.model.audio.Track
import kotlinx.coroutines.flow.StateFlow

/**
 * Interfaz para operaciones de múltiples pistas de audio.
 */
interface AudioMixerRepository {
    /**
     * Reproduce las pistas.
     */
    fun play()

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
    fun createTrack()

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
    suspend fun getTracks(): StateFlow<List<Track>>


    /**
     * Verifica si todas las pistas han sido reproducidas.
     */
    suspend fun allTracksWerePlayed(): StateFlow<Boolean>
}
