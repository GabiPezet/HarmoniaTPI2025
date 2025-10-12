package com.android.harmoniatpi.domain.usecases

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class GenerateWaveformUseCaseTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var useCase: GenerateWaveformUseCase

    @Test
    fun `invoke with valid PCM file returns normalized waveform`() {
        // Preparamos un archivo temporal con datos PCM de prueba.
        // 512 bytes -> 256 shorts. Con 256 samples por pico, esto debería generar 1 ventana y 2 picos.
        val fakePcmData = ByteArray(512) { i -> (i % 256 - 128).toByte() }
        val tempFile: File = temporaryFolder.newFile("test.pcm")
        tempFile.writeBytes(fakePcmData)

        useCase = GenerateWaveformUseCase()

        // Ejecutamos el caso de uso con la ruta del archivo temporal
        val waveform = useCase(tempFile.absolutePath)

        // Verificamos el resultado
        // El tamaño esperado es de 2 picos (máximo y mínimo)
        assertEquals(2, waveform.size)

        // Verificamos que todos los valores estén en el rango normalizado de -1.0 a 1.0
        waveform.forEach { value ->
            assertTrue("Value $value should be <= 1.0f", value <= 1.0f)
            assertTrue("Value $value should be >= -1.0f", value >= -1.0f)
        }
    }

    @Test
    fun `invoke with empty file returns empty list`() {
        val tempFile: File = temporaryFolder.newFile("empty.pcm")
        tempFile.writeBytes(byteArrayOf())

        useCase = GenerateWaveformUseCase()

        val waveform = useCase(tempFile.absolutePath)

        assertTrue(waveform.isEmpty())
    }
}
