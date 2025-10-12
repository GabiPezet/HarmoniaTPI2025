package com.android.harmoniatpi.domain.interfaces

/**
 * Interfaz para reproducir audio.
 */
interface AudioPlayer {
    /**
     * Reproduce el audio desde el archivo especificado mediante [setFile].
     */
    fun play(): Result<Unit>

    /**
     * Pausa la reproducción del audio.
     */
    fun pause()

    /**
     * Para la reproducción del audio.
     */
    fun stop()

    /**
     * Libera los recursos del reproductor.
     */
    fun release()

    /**
     * Establece el archivo de audio a reproducir.
     * @param path Ruta del archivo de audio.
     */
    fun setFile(path: String)

    /**
     * Establece un callback que se ejecutará cuando se complete la reproducción del audio.
     */
    fun setOnPlaybackCompletedCallback(callback: () -> Unit)
}