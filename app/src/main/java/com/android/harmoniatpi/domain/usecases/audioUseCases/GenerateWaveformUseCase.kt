package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.model.audio.WaveformResult
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import kotlin.math.abs

class GenerateWaveformUseCase @Inject constructor() {

    // Cuántas muestras de PCM sin procesar se procesan para producir un par de picos mínimo/máximo.
    // Un número mayor significa se crea un waveform más rápidamente pero con menos detalles.
    // 256 es un buen valor por defecto.
    private val SAMPLES_PER_PEAK = 256

    // El tamaño del búfer para leer del archivo de una sola vez (en bytes).
    // Debería ser un múltiplo de (SAMPLES_PER_PEAK * 2 bytes/muestra * 2 para min/max).
    // 8KB es un tamaño robusto y eficiente.
    private val BUFFER_SIZE = 8 * 1024

    operator fun invoke(path: String): WaveformResult {
        val file = File(path)
        if (!file.exists() || file.length() == 0L) {
            return WaveformResult(emptyList(), 0L)
        }

        val totalSamples = file.length() / 2
        val durationMs = (totalSamples * 1000L) / 44100L

        val peaks = generatePeaks(file)
        val normalizedWaveform = normalizePeaks(peaks)

        return WaveformResult(normalizedWaveform, durationMs)
    }

    private fun generatePeaks(file: File): List<Float> {
        val peaks = mutableListOf<Float>()

        FileInputStream(file).use { fis ->
            val buffer = ByteArray(BUFFER_SIZE)

            val window = ShortArray(SAMPLES_PER_PEAK)
            var windowIndex = 0

            while (fis.read(buffer) != -1) {
                val shortBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()

                while (shortBuffer.hasRemaining()) {
                    window[windowIndex++] = shortBuffer.get()

                    if (windowIndex == SAMPLES_PER_PEAK) {
                        var minPeak: Short = Short.MAX_VALUE
                        var maxPeak: Short = Short.MIN_VALUE

                        for (sample in window) {
                            if (sample > maxPeak) maxPeak = sample
                            if (sample < minPeak) minPeak = sample
                        }

                        peaks.add(maxPeak.toFloat())
                        peaks.add(minPeak.toFloat())

                        windowIndex = 0
                    }
                }
            }
        }
        return peaks
    }

    private fun normalizePeaks(peaks: List<Float>): List<Float> {
        if (peaks.isEmpty()) return emptyList()

        val maxAbsValue = peaks.maxOfOrNull { abs(it) } ?: 1f

        if (maxAbsValue == 0f) return peaks.map { 0f }

        val normalizedPeaks = MutableList(peaks.size) { 0f }
        for (i in peaks.indices) {
            normalizedPeaks[i] = peaks[i] / maxAbsValue
        }
        return normalizedPeaks
    }
}