package com.android.harmoniatpi.data.audio.utility

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavUtilityTest {

    private lateinit var wavUtility: WavUtility

    @Before
    fun setUp() {
        wavUtility = WavUtility()
    }

    @Test
    fun `createWavHeader con parametros validos retorna header correcto`() {
        val pcmSize = 176400 // 2 segundos de audio mono a 44100 Hz, 16-bit
        val sampleRate = 44100
        val numChannels = 1
        val bitsPerSample = 16
        val expectedByteRate = sampleRate * numChannels * bitsPerSample / 8
        val expectedBlockAlign = (numChannels * bitsPerSample / 8).toShort()

        val generatedHeader = wavUtility.createWavHeader(
            pcmSize = pcmSize,
            sampleRate = sampleRate,
            numChannels = numChannels,
            bitsPerSample = bitsPerSample
        )

        assertEquals("El header debe tener 44 bytes", 44, generatedHeader.size)

        // podemos leer cómodamente el ByteArray desde un ByteBuffer
        val buffer = ByteBuffer.wrap(generatedHeader)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        // RIFF
        val riffId = ByteArray(4).also { buffer.get(it) }
        assertArrayEquals(
            "ChunkID debe ser 'RIFF'",
            "RIFF".toByteArray(Charsets.US_ASCII),
            riffId
        )
        assertEquals(
            "ChunkSize debe ser pcmSize + 36",
            pcmSize + 36,
            buffer.int
        )
        val waveId = ByteArray(4).also { buffer.get(it) }
        assertArrayEquals(
            "Format debe ser 'WAVE'",
            "WAVE".toByteArray(Charsets.US_ASCII),
            waveId
        )

        // fmt Sub-chunk
        val fmtId = ByteArray(4).also { buffer.get(it) }
        assertArrayEquals(
            "Subchunk1ID debe ser 'fmt '",
            "fmt ".toByteArray(Charsets.US_ASCII),
            fmtId
        )
        assertEquals(
            "Subchunk1Size debe ser 16",
            16,
            buffer.int
        )
        assertEquals(
            "AudioFormat debe ser 1 (PCM)",
            1.toShort(),
            buffer.short
        )
        assertEquals(
            "NumChannels debe ser 1",
            numChannels.toShort(),
            buffer.short
        )
        assertEquals(
            "SampleRate debe ser 44100",
            sampleRate,
            buffer.int
        )
        assertEquals(
            "ByteRate debe ser el esperado",
            expectedByteRate,
            buffer.int
        )
        assertEquals(
            "BlockAlign debe ser el esperado",
            expectedBlockAlign,
            buffer.short
        )
        assertEquals(
            "BitsPerSample debe ser 16",
            bitsPerSample.toShort(),
            buffer.short
        )
    }

    @Test
    fun `createWavHeader larga una excepcion cuando pcmSize es cero`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            wavUtility.createWavHeader(
                pcmSize = 0,
                sampleRate = 44100,
                numChannels = 1,
                bitsPerSample = 16
            )
        }
        assertEquals("El tamaño del PCM debe ser mayor que cero", exception.message)
    }

    @Test
    fun `createWavHeader larga una excepcion cuando sampleRate es cero`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            wavUtility.createWavHeader(
                pcmSize = 1024,
                sampleRate = 0,
                numChannels = 1,
                bitsPerSample = 16
            )
        }
        assertEquals(
            "La tasa de muestreo debe ser mayor que cero",
            exception.message
        )
    }

    @Test
    fun `createWavHeader larga una excepcion cuando numChannels es cero`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            wavUtility.createWavHeader(
                pcmSize = 1024,
                sampleRate = 44100,
                numChannels = 0,
                bitsPerSample = 16
            )
        }
        assertEquals(
            "El número de canales debe ser mayor que cero",
            exception.message
        )
    }

    @Test
    fun `createWavHeader larga una excepcion cuando se usan bitsPerSample que no son soportados`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            // Usamos un valor no soportado, como 12 o 24
            wavUtility.createWavHeader(
                pcmSize = 1024,
                sampleRate = 44100,
                numChannels = 1,
                bitsPerSample = 12
            )
        }
        assertEquals("Los bits por muestra deben ser 8 o 16", exception.message)
    }

    @Test
    fun `createWavHeader larga una excepcion cuando pcmSize no es multiplo de blockAlign`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            // Para audio estéreo (2 canales) de 16-bit, blockAlign es 4.
            // 1025 no es múltiplo de 4, por lo que debería fallar.
            wavUtility.createWavHeader(
                pcmSize = 1025,
                sampleRate = 44100,
                numChannels = 2,
                bitsPerSample = 16
            )
        }
        assertEquals(
            "El tamaño del PCM debe ser múltiplo del número de canales y bits por muestra",
            exception.message
        )
    }
}
