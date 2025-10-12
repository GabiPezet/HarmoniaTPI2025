package com.android.harmoniatpi.domain.interfaces

/**
 * Interfaz para operaciones de grabación de audio.
 */
interface AudioRecorderRepository {
    /**
     * Inicia la grabación de audio.
     * @param outputFilePath Ruta del archivo de salida.
     */
    fun startRecording(outputFilePath: String): Result<Unit>

    /**
     * Para la grabación de audio.
     */
    fun stopRecording(): Result<Unit>
}