package com.android.harmoniatpi.domain.interfaces

import kotlinx.coroutines.flow.SharedFlow

/**
 * Interfaz para operaciones de grabación de audio.
 */
interface AudioRecorderRepository {
    /**
     * Inicia la grabación de audio.
     * @param outputFilePath Ruta del archivo de salida.
     */
    fun startRecording(outputFilePath: String, audioSource: Int): Result<Unit>

    /**
     * Para la grabación de audio.
     */
    fun stopRecording(): Result<Unit>

    /**
     * Obtiene un waveform en vivo
     */
    fun getLiveWaveform(): SharedFlow<List<Float>>
}