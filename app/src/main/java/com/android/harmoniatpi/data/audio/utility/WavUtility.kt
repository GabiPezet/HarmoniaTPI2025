package com.android.harmoniatpi.data.audio.utility

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
}