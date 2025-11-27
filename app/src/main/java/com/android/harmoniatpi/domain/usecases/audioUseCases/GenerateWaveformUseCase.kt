package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.model.audio.WaveformResult
import java.io.File
import javax.inject.Inject
import kotlin.math.abs

class GenerateWaveformUseCase @Inject constructor() {

    operator fun invoke(path: String): WaveformResult {
        val file = File(path)

        if (!file.exists() || file.length() == 0L) {
            return WaveformResult(emptyList(), 0L)
        }

        // Configuración de ventana (Downsampling)
        val samplesPerPeak = 256
        val peaks = mutableListOf<Float>()

        var currentMax = Short.MIN_VALUE
        var currentMin = Short.MAX_VALUE
        var sampleCount = 0

        // Para normalización final
        var globalMaxAbs = 0f
        var totalBytesRead = 0L

        try {
            //Leer en stream
            file.inputStream().buffered().use { inputStream ->
                val buffer = ByteArray(8192) // Buffer de 8KB (4096 muestras)
                var bytesRead = inputStream.read(buffer)

                while (bytesRead != -1) {
                    totalBytesRead += bytesRead

                    //Procesar el buffer actual
                    for (i in 0 until bytesRead step 2) {
                        if (i + 1 < bytesRead) {
                            val low = buffer[i].toInt() and 0xFF
                            val high = buffer[i + 1].toInt()
                            val sample = ((high shl 8) or low).toShort()

                            //Lógica de Picos (Min/Max en la ventana)
                            if (sample > currentMax) currentMax = sample
                            if (sample < currentMin) currentMin = sample

                            sampleCount++

                            //Si se llenó la ventana, guardamos los picos y reseteamos
                            if (sampleCount >= samplesPerPeak) {
                                val maxVal = currentMax.toFloat()
                                val minVal = currentMin.toFloat()

                                peaks.add(maxVal)
                                peaks.add(minVal)

                                val absMax = abs(maxVal)
                                val absMin = abs(minVal)
                                if (absMax > globalMaxAbs) globalMaxAbs = absMax
                                if (absMin > globalMaxAbs) globalMaxAbs = absMin

                                currentMax = Short.MIN_VALUE
                                currentMin = Short.MAX_VALUE
                                sampleCount = 0
                            }
                        }
                    }
                    bytesRead = inputStream.read(buffer)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return WaveformResult(emptyList(), 0L)
        }

        val normalizedWaveform = if (globalMaxAbs > 0) {
            peaks.map { it / globalMaxAbs }
        } else {
            peaks.map { 0f }
        }

        val totalSamples = totalBytesRead / 2
        val sampleRate = 44100L
        val durationMs = (totalSamples * 1000L) / sampleRate

        return WaveformResult(normalizedWaveform, durationMs)
    }
}