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
     * Obtiene las pistas actuales.
     */
    suspend fun getTracks(): StateFlow<List<Track>>

    /**
     * Verifica si todas las pistas han sido reproducidas.
     */
    suspend fun allTracksWerePlayed(): StateFlow<Boolean>
}
