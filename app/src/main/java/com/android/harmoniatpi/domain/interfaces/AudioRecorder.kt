package com.android.harmoniatpi.domain.interfaces

import kotlinx.coroutines.flow.SharedFlow

/**
 * Interfaz para operaciones de grabación de audio.
 */
interface AudioRecorder {
    /**
     * Establece el archivo de salida para la grabación. Este luego se usará para reproducir la pista.
     * @param path Ruta del archivo de salida.
     */
    fun setOutputFile(path: String)

    /**
     * Inicia la grabación de audio.
     */
    fun startRecording(audioSource: Int): Result<Unit>

    /**
     * Para la grabación de audio.
     */
    fun stopRecording(): Result<Unit>

    fun getLiveWaveform(): SharedFlow<List<Float>>
}