package com.android.harmoniatpi.data.audio.player

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter
import be.tarsos.dsp.io.TarsosDSPAudioFormat

/**
 * Un procesador de audio ([AudioProcessor]) que reproduce el flujo de audio procesado por TarsosDSP
 * utilizando el sistema de [AudioTrack] de Android.
 *
 * Esta clase actúa como un "Sink" (sumidero) o punto final en la cadena de procesamiento,
 * convirtiendo los datos de punto flotante de TarsosDSP de vuelta a bytes PCM para ser
 * reproducidos por el hardware del dispositivo.
 *
 * @property tarsosFormat El formato de audio que utiliza TarsosDSP (frecuencia de muestreo, canales, etc.).
 * @property bufferSizeInBytes El tamaño deseado del buffer en bytes. Por defecto es 4096.
 * Si el hardware requiere un buffer mayor, se usará el mínimo requerido por el sistema.
 * @property streamType El tipo de stream de Android (ej. [AudioManager.STREAM_MUSIC]). Por defecto es MÚSICA.
 */
class AndroidAudioPlayer(
    private val tarsosFormat: TarsosDSPAudioFormat,
    bufferSizeInBytes: Int = 4096,
    streamType: Int = AudioManager.STREAM_MUSIC
) : AudioProcessor {

    /**
     * Instancia de AudioTrack de Android encargada de la reproducción de bajo nivel.
     */
    private val audioTrack: AudioTrack

    /**
     * Convertidor auxiliar para transformar los arrays de Floats (usados por Tarsos para efectos)
     * a arrays de Bytes (necesarios para AudioTrack).
     */
    private val converter: TarsosDSPAudioFloatConverter = TarsosDSPAudioFloatConverter.getConverter(tarsosFormat)

    /**
     * Buffer intermedio donde se almacenan los bytes convertidos antes de enviarlos al AudioTrack.
     */
    private val byteBuffer: ByteArray = ByteArray(bufferSizeInBytes)

    init {

        // Configuración del AudioTrack
        val sampleRate = tarsosFormat.sampleRate.toInt()

        // Convertir formato Tarsos a configuración de canales de Android
        val channelConfig = if (tarsosFormat.channels == 1) {
            AudioFormat.CHANNEL_OUT_MONO
        } else {
            AudioFormat.CHANNEL_OUT_STEREO
        }

        // Calcular tamaño mínimo del buffer requerido por el hardware de Android
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            channelConfig,
            AudioFormat.ENCODING_PCM_16BIT
        )

        // Usar el mayor entre el buffer solicitado y el mínimo del sistema para evitar latencia o cortes
        val finalBufferSize = maxOf(minBufferSize, bufferSizeInBytes)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setLegacyStreamType(streamType)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(channelConfig)
                    .build()
            )
            .setBufferSizeInBytes(finalBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        try {
            // Inicia el AudioTrack en estado de espera para recibir datos
            audioTrack.play()
        } catch (e: IllegalStateException) {
            Log.e("AndroidAudioPlayer", "Error al iniciar AudioTrack: ${e.message}")
        }
    }

    /**
     * Procesa un evento de audio entrante desde la cadena de TarsosDSP.
     *
     * Este método toma el buffer de floats del [audioEvent], lo convierte a PCM de 16-bits (bytes)
     * y lo escribe en el [audioTrack] para su reproducción inmediata.
     *
     * @param audioEvent El objeto que contiene los datos de audio procesados (buffer de floats).
     * @return `true` para indicar que el procesamiento fue exitoso y la cadena puede continuar.
     */
    override fun process(audioEvent: AudioEvent): Boolean {
        // 1. Convertir el buffer de floats (procesado por efectos) a bytes
        converter.toByteArray(audioEvent.floatBuffer, byteBuffer)

        // 2. Calcular cuántos bytes válidos debemos escribir.
        // Se calcula multiplicando la cantidad de samples en el buffer por el tamaño en bytes de cada frame.
        // Esto reemplaza el uso de la propiedad privada 'bytesProcessing'.
        val bytesToWrite = audioEvent.bufferSize * tarsosFormat.frameSize

        try {
            // 3. Escribir en el AudioTrack para que suene
            audioTrack.write(byteBuffer, 0, bytesToWrite)
        } catch (e: Exception) {
            Log.e("AndroidAudioPlayer", "Error escribiendo audio", e)
        }

        return true
    }

    /**
     * Se llama cuando la cadena de procesamiento ha finalizado (ej. fin del archivo o stop manual).
     *
     * Detiene la reproducción y libera los recursos nativos del [AudioTrack] para evitar fugas de memoria.
     */
    override fun processingFinished() {
        try {
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            Log.e("AndroidAudioPlayer", "Error al liberar recursos", e)
        }
    }
}