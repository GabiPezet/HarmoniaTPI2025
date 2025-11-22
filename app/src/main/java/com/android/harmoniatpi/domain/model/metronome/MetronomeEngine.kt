package com.android.harmoniatpi.domain.model.metronome // O donde prefieras

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.android.harmoniatpi.R // Importa tu R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetronomeEngine @Inject constructor(
    @ApplicationContext context: Context
) {
    private val soundPool: SoundPool
    private var soundId: Int = 0
    private var isSoundLoaded = false
    private var executor: ScheduledExecutorService? = null

    @Volatile
    private var bpm: Int = 120
    @Volatile
    private var isSoundEnabled: Boolean = false
    @Volatile
    private var isPlaying: Boolean = false
    @Volatile
    private var currentVolume: Float = 1.0f

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { pool, sampleId, status ->
            if (status == 0) {
                isSoundLoaded = true
                soundId = sampleId
                Log.i("MetronomeEngine", "Metronome tick cargado exitosamente.")
            } else {
                Log.e("MetronomeEngine", "Error al cargar el sonido del metrónomo, status: $status")
            }
        }

        // el sonido (esto dispara el listener de arriba)
        soundPool.load(context, R.raw.metronome_wood_high, 1)
    }

    fun start() {
        if (isPlaying) return
        isPlaying = true
        startExecutor()
    }

    fun stop() {
        if (!isPlaying) return
        isPlaying = false
        stopExecutor()
    }

    /**
     * Reproduce el sonido del tick UNA VEZ, si el sonido está cargado y habilitado.
     * Útil para la pre-cuenta.
     */
    fun playTick() {
        if (isSoundEnabled && isSoundLoaded) {
            val vol = currentVolume
            soundPool.play(soundId, vol, vol, 0, 0, 1f)
            Log.d("MetronomeEngine", "Pre-count tick sonó a volumen $vol")
        }
    }

    fun setBpm(newBpm: Int) {
        bpm = newBpm
        if (isPlaying) {
            stopExecutor()
            startExecutor()
        }
    }

    fun setSoundEnabled(isEnabled: Boolean) {
        isSoundEnabled = isEnabled
    }

    /**
     * Actualiza el volumen del metrónomo.
     * Esta es la función que llama el ViewModel.
     */
    fun setVolume(newVolume: Float) {
        // Actualizamos nuestra variable @Volatile
        currentVolume = newVolume.coerceIn(0.0f, 1.0f)
        Log.d("MetronomeEngine", "Volumen actualizado a: $currentVolume")
    }

    fun release() {
        soundPool.release()
        stop()
    }

    /**
     * Inicia el executor que reproduce el tick del metrónomo.
     */
    private fun startExecutor() {
        if (executor != null) return
        val delayMs = 60_000L / bpm

        executor = Executors.newSingleThreadScheduledExecutor()
        executor?.scheduleWithFixedDelay(
            {
                if (isSoundEnabled && isSoundLoaded) {
                    val vol = currentVolume
                    soundPool.play(soundId, vol, vol, 0, 0, 1f)
                }
            },
            0,
            delayMs,
            TimeUnit.MILLISECONDS
        )
    }

    private fun stopExecutor() {
        executor?.shutdownNow() // Usamos shutdownNow() para ser más inmediatos
        executor = null
    }
}