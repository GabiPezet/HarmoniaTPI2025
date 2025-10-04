package com.android.harmoniatpi.data.audio.utility

import javax.inject.Inject

class WavUtility @Inject constructor() {


    companion object {
        private const val HEADER_SIZE = 44
        private val CHUNK_ID = "RIFF".toByteArray(Charsets.US_ASCII)
        private val FORMAT = "WAVE".toByteArray(Charsets.US_ASCII)
        private val SUB_CHUNK1_ID = "fmt ".toByteArray(Charsets.US_ASCII)
        private const val SUB_CHUNK1_SIZE = 16
        private const val AUDIO_FORMAT = 1
        private val SUB_CHUNK2_ID = "data".toByteArray(Charsets.US_ASCII)
        private const val BYTE_BIT_SIZE = 8
        private const val SUB_HEADER_SIZE = HEADER_SIZE - 8
    }

    fun createWavHeader(pcmSize: Int, sampleRate: Int, numChannels: Int, bitsPerSample: Int): ByteArray {
        val header = ByteArray(HEADER_SIZE)
        val size = SUB_HEADER_SIZE + pcmSize
        val byteRate = sampleRate * numChannels * bitsPerSample / BYTE_BIT_SIZE
        val byteCount = numChannels * bitsPerSample / BYTE_BIT_SIZE

        // se crea el header


        // se añade riff al header
        header[0] = CHUNK_ID[0]
        header[1] = CHUNK_ID[1]
        header[2] = CHUNK_ID[2]
        header[3] = CHUNK_ID[3]

        // se agrega el tamaño del pcm + el tamaño del header menos 8 bytes
        // (se ignoran los bytes en donde se guardó RIFF
        // estos cálculos raros es para convertir un int a little endian
        header[4] = (size and 0xFF).toByte()
        header[5] = (size shr 8 and 0xFF).toByte()
        header[6] = (size shr 16 and 0xFF).toByte()
        header[7] = (size shr 24 and 0xFF).toByte()

        // se agrega el formato, en nuestro caso WAVE
        header[8] = FORMAT[0]
        header[9] = FORMAT[1]
        header[10] = FORMAT[2]
        header[11] = FORMAT[3]

        // se agrega subchunkid al header, en nuestro caso "fmt "
        header[12] = SUB_CHUNK1_ID[0]
        header[13] = SUB_CHUNK1_ID[1]
        header[14] = SUB_CHUNK1_ID[2]
        header[15] = SUB_CHUNK1_ID[3]

        // se agrega subchunksize al header, en nuestro caso 16 porque usamos pcm
        header[16] = (SUB_CHUNK1_SIZE and 0xFF).toByte()
        header[17] = (SUB_CHUNK1_SIZE shr 8 and 0xFF).toByte()
        header[18] = (SUB_CHUNK1_SIZE shr 16 and 0xFF).toByte()
        header[19] = (SUB_CHUNK1_SIZE shr 24 and 0xFF).toByte()

        // se agrega el auioformat al header, en nuestro caso 1
        // otros valores aparte del 1 indican que el pcm está comprimido de alguna manera
        header[20] = (AUDIO_FORMAT and 0xFF).toByte()
        header[21] = (AUDIO_FORMAT shr 8 and 0xFF).toByte()

        // se agrega el numero de canales al header, mono = 1, stereo = 2, etc.
        header[22] = (numChannels and 0xFF).toByte()
        header[23] = (numChannels shr 8 and 0xFF).toByte()

        // frecuencia de muestreo
        header[24] = (sampleRate and 0xFF).toByte()
        header[25] = (sampleRate shr 8 and 0xFF).toByte()
        header[26] = (sampleRate shr 16 and 0xFF).toByte()
        header[27] = (sampleRate shr 24 and 0xFF).toByte()

        // se agrega el byterate al header
        header[28] = (byteRate and 0xFF).toByte()
        header[29] = (byteRate shr 8 and 0xFF).toByte()
        header[30] = (byteRate shr 16 and 0xFF).toByte()
        header[31] = (byteRate shr 24 and 0xFF).toByte()

        // se agrega blockaling
        header[32] = (byteCount and 0xFF).toByte()
        header[33] = (byteCount shr 8 and 0xFF).toByte()

        // se agrega bitsPerSample
        header[34] = (bitsPerSample and 0xFF).toByte()
        header[35] = (bitsPerSample shr 8 and 0xFF).toByte()

        // se agrega subchunk2id, contiene el string "data"
        header[36] = SUB_CHUNK2_ID[0]
        header[37] = SUB_CHUNK2_ID[1]
        header[38] = SUB_CHUNK2_ID[2]
        header[39] = SUB_CHUNK2_ID[3]

        // se agrega subchunk2size
        header[40] = (pcmSize and 0xFF).toByte()
        header[41] = (pcmSize shr 8 and 0xFF).toByte()
        header[42] = (pcmSize shr 16 and 0xFF).toByte()
        header[43] = (pcmSize shr 24 and 0xFF).toByte()

        return header
    }
}