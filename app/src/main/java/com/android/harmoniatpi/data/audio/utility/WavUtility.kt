package com.android.harmoniatpi.data.audio.utility

import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

class WavUtility @Inject constructor() {

    companion object {
        private const val WAV_HEADER_SIZE = 44
        private const val BITS_PER_BYTE = 8
        private const val SUB_CHUNK_1_SIZE = 16
    }

    /**
     * Crea un header/encabezado de un Wav que pesa 44 bytes.
     *
     * Este tipo de encabezado usa little endian para todos los valores numéricos, excepto
     * los chars y el PCM en si.
     *
     * Los chars, en formato ASCII, solamente ocupan 1 byte cada uno, y no necesitan tratamiento
     * especial. Little/big endian solamente aplica a valores que ocupen más de 1 byte.
     *
     * @param pcmSize El tamaño del PCM en bytes (Subchunk2Size).
     * @param sampleRate La tasa de muestreo del audio (por ej., 44100).
     * @param numChannels El número de canales (1 para mono, 2 para estéreo, etc.).
     * @param bitsPerSample El número de bits por muestra (por ej., 16 para audio de 16 bits).
     * @return Un ByteArray que contiene el encabezado WAV.
     */
    fun createWavHeader(pcmSize: Int, sampleRate: Int, numChannels: Int, bitsPerSample: Int): ByteArray {
        require(pcmSize > 0) { "El tamaño del PCM debe ser mayor que cero" }
        require(sampleRate > 0) { "La tasa de muestreo debe ser mayor que cero" }
        require(numChannels > 0) { "El número de canales debe ser mayor que cero" }
        require(bitsPerSample > 0) { "Los bits por muestra deben ser mayor que cero" }
        require(bitsPerSample == 8 || bitsPerSample == 16) { "Los bits por muestra deben ser 8 o 16"}
        require(pcmSize % (numChannels * bitsPerSample / BITS_PER_BYTE) == 0) {
            "El tamaño del PCM debe ser múltiplo del número de canales y bits por muestra"
        }

        val header = ByteBuffer.allocate(WAV_HEADER_SIZE)
        val byteRate = sampleRate * numChannels * bitsPerSample / BITS_PER_BYTE
        val blockAlign = (numChannels * bitsPerSample / BITS_PER_BYTE).toShort()
        val chunkSize = pcmSize + WAV_HEADER_SIZE - 8 // se resta 8 bytes porque se ignora ChunkID y ChunkSize

        header.order(ByteOrder.LITTLE_ENDIAN)

        // RIFF chunk (12 bytes)
        // ChunkID (4 bytes)
        // El uso de Charsets.US_ASCII hace que cada byte contenga 1 char
        header.put("RIFF".toByteArray(Charsets.US_ASCII))
        // ChunkSize (4 bytes)
        header.putInt(chunkSize)
        // Format (4 bytes)
        header.put("WAVE".toByteArray(Charsets.US_ASCII))

        // "fmt " sub-chunk (24 bytes)
        // Subchunk1ID (4 bytes)
        header.put("fmt ".toByteArray(Charsets.US_ASCII))
        // Subchunk1Size para un PCM (4 bytes)
        header.putInt(SUB_CHUNK_1_SIZE)
        // AudioFormat (1 para PCM) (2 bytes)
        header.putShort(1.toShort())
        // NumChannels (2 bytes)
        header.putShort(numChannels.toShort())
        // SampleRate (4 bytes)
        header.putInt(sampleRate)
        // ByteRate (4 bytes)
        header.putInt(byteRate)
        // BlockAlign (2 bytes)
        header.putShort(blockAlign)
        // BitsPerSample (2 bytes)
        header.putShort(bitsPerSample.toShort())

        // "data" sub-chunk (8 bytes)
        // Subchunk2ID (4 bytes)
        header.put("data".toByteArray(Charsets.US_ASCII))
        // Subchunk2Size (4 bytes)
        header.putInt(pcmSize)

        return header.array()
    }

    /**
     * Escribe un archivo WAV completo (header + PCM) a un OutputStream.
     *
     * Este método es eficiente en memoria ya que escribe los datos directamente
     * al stream de salida sin crear un array de bytes combinado en memoria.
     * Es ideal para archivos grandes.
     *
     * @param pcm Los datos de audio PCM en formato de ByteArray.
     * @param outputStream El stream donde se escribirá el archivo WAV.
     * @param sampleRate La tasa de muestreo del audio.
     * @param numChannels El número de canales.
     * @param bitsPerSample Los bits por muestra.
     */
    fun writeWavToStream(
        pcm: ByteArray,
        outputStream: OutputStream,
        sampleRate: Int,
        numChannels: Int,
        bitsPerSample: Int
    ) {
        val header = createWavHeader(
            pcmSize = pcm.size,
            sampleRate = sampleRate,
            numChannels = numChannels,
            bitsPerSample = bitsPerSample
        )
        // 'use' se asegura de que el stream se cierre correctamente,
        // incluso si ocurren errores.
        // no hay necesidad de usar .flush() o .close()
        outputStream.use { out ->
            out.write(header)
            out.write(pcm)
        }
    }

    /**
     * Crea un archivo WAV a partir de un ByteArray de PCM.
     *
     * Este método es más simple de usar que writeWavToStream y, tal vez,
     * sea más rápido para PCM pequeños o que necesitemos editar en memoria.
     * Si se quiere guardar un Wav grande, es mejor usar writeWavToStream.
     *
     * @param pcm Los datos de audio PCM en formato de ByteArray.
     * @param sampleRate La tasa de muestreo del audio.
     * @param numChannels El número de canales.
     * @param bitsPerSample Los bits por muestra.
     *
     */
    fun createWav(pcm: ByteArray, sampleRate: Int, numChannels: Int, bitsPerSample: Int): ByteArray {
        val header = createWavHeader(
            pcmSize = pcm.size,
            sampleRate = sampleRate,
            numChannels = numChannels,
            bitsPerSample = bitsPerSample
        )
        val wav = ByteBuffer.allocate(header.size + pcm.size)
        wav.put(header)
        wav.put(pcm)
        return wav.array()
    }
}