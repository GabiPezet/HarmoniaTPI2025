package com.android.harmoniatpi.domain.interfaces

import kotlinx.coroutines.flow.Flow

/**
 * Interfaz para operaciones de reproducción de audio utilizando ExoPlayer.
 */

interface ExoAudioPlayerRepository {
    /**
     * Reproduce el audio desde la URL especificada.
     * @param audioUrl URL del audio a reproducir.
     */
    fun play(audioUrl: String)

    /**
     * Pausa la reproducción del audio.
     */
    fun pause()

    /**
     * Para la reproducción del audio.
     */
    fun stop()

    /**
     * Salta a una posición especifica en el audio
     */
    fun seekTo(positionMs: Long)

    /**
     * Expone la posición actual del reproductor como un flujo de datos en tiempo real.
     **/
    fun getCurrentPositionMs(): Flow<Long>

    /**
     * Expone la duración total del audio actual como un flujo de datos.
     * Emite 0L si no hay audio cargado o la duración es desconocida.
     */
    fun getTotalDurationMs(): Flow<Long>
}