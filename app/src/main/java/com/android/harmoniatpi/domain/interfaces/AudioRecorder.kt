package com.android.harmoniatpi.domain.interfaces

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
    fun startRecording(): Result<Unit>

    /**
     * Para la grabación de audio.
     */
    fun stopRecording(): Result<Unit>
}