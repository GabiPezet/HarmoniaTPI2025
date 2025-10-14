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
     * Reproduce un segmento del audio desde el archivo especificado mediante [setFile].
     * @param startMs Tiempo de inicio del segmento en milisegundos.
     * @param endMs Tiempo de fin del segmento en milisegundos.
     */
    fun playSegment(startMs: Long, endMs: Long): Result<Unit>


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

    /**
     * Mutea el audio.
     */
    fun mute()

    /**
     * Desmutea el audio.
     */
    fun unMute()

    /**
     * Devuelve el estado de muteo del audio.
     * @return true si el audio está muteado, false en caso contrario.
     */
    fun isMuted(): Boolean

    /**
     * Modifica el volumen del audio.
     * @param volume Valor entre 0 y 1 que representa el volumen.
     */
    fun setVolume(volume: Float)

    /**
     * Devuelve el volumen actual del audio.
     * @return Valor entre 0 y 1 que representa el volumen.
     */
    fun getVolume(): Float
}