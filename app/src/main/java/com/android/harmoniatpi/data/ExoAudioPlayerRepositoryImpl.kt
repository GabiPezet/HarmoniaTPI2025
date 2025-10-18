package com.android.harmoniatpi.data

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import com.android.harmoniatpi.domain.interfaces.ExoAudioPlayerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de reproducción de audio utilizando ExoPlayer.
 */

@Singleton
class ExoAudioPlayerRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ExoAudioPlayerRepository {
    private var exoPlayer: ExoPlayer? = ExoPlayer.Builder(context).build()

    private val _durationMs = MutableStateFlow(0L)

    init {
        /**
         * Listener para el reproductor de ExoPlayer. Detecta la duración
         */
        exoPlayer?.addListener(object : Player.Listener {
            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                val duration = exoPlayer?.duration ?: 0L
                if (duration > 0) {
                    _durationMs.value = duration
                }
            }
        })
    }

    /**
     * Reproduce el audio desde la URL especificada.
     * @param audioUrl URL del audio a reproducir.
     */
    override fun play(audioUrl: String) {
        // Prepara el item de medios desde la URL
        val mediaItem = MediaItem.fromUri(audioUrl)

        exoPlayer?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    /**
     * Pausa la reproducción del audio.
     */
    override fun pause() {
        exoPlayer?.pause()
    }

    /**
     * Para la reproducción del audio.
     */
    override fun stop() {
        exoPlayer?.stop()
        _durationMs.value =0L
    }

    /**
     * Salta a una posición especifica en el audio.
     * @param positionMs Posición en milisegundos a la que saltar.
     */
    override fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    /**
     * Expone la posición actual del reproductor como un flujo de datos en tiempo real.
     */
    override fun getCurrentPositionMs(): Flow<Long> {
        return flow {
            // Este bucle se ejecutará mientras el Flow esté siendo recolectado
            while (currentCoroutineContext().isActive) {
                val currentPosition = exoPlayer?.currentPosition ?: 0L
                emit(currentPosition)
                delay(100L)
            }
        }.flowOn(Dispatchers.Main)
    }

    /**
     * Expone la duración total del audio actual como un flujo de datos.
     */
    override fun getTotalDurationMs(): Flow<Long> {
        return _durationMs.asStateFlow()
    }

    /**
     * Libera los recursos del reproductor.
     */
    fun release() {
        exoPlayer?.release()
        exoPlayer = null
    }

}