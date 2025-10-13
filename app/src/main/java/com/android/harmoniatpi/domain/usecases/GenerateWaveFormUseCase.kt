package com.android.harmoniatpi.domain.usecases

import java.io.File
import javax.inject.Inject

data class WaveformResult(
    val waveform: List<Float>,
    val durationMs: Long
)

class GenerateWaveformUseCase @Inject constructor() {

    // Función principal modificada para devolver WaveformResult
    operator fun invoke(path: String): WaveformResult {
        val file = File(path)

        // compruebo si el archivo existe y tiene contenido antes de leerlo
        if (!file.exists() || file.length() == 0L) {
            return WaveformResult(emptyList(), 0L)
        }

        val pcmBytes = file.readBytes()

        val pcmShortArray = byteArrayToShortArray(pcmBytes)
        val waveform = pcmShortArrayToNormalizedWaveform(pcmShortArray)

        // CÁLCULO DE DURACIÓN (44100 muestras/segundo)
        val totalSamples = pcmShortArray.size.toLong()
        val sampleRate = 44100L
        val durationMs = (totalSamples * 1000L) / sampleRate

        return WaveformResult(waveform, durationMs)
    }

    private fun byteArrayToShortArray(pcmBytes: ByteArray): ShortArray {
        val shorts = ShortArray(pcmBytes.size / 2)
        for (i in shorts.indices) {
            val low = pcmBytes[i * 2].toInt() and 0xFF
            val high = pcmBytes[i * 2 + 1].toInt()
            shorts[i] = ((high shl 8) or low).toShort()
        }
        return shorts
    }

    private fun pcmShortArrayToNormalizedWaveform(
        pcm: ShortArray,
        samplesPerPeak: Int = 256 // Cada cuantos samples se tomará un pico
    ): List<Float> {
        if (pcm.isEmpty()) return emptyList()

        val numWindows = pcm.size / samplesPerPeak
        val peaks = mutableListOf<Float>()

        for (i in 0 until numWindows) {
            val start = i * samplesPerPeak
            val end = (start + samplesPerPeak).coerceAtMost(pcm.size)
            if (start >= end) continue

            // Tomamos una "ventana" de la señal de audio
            val window = pcm.slice(start until end)

            // Encontramos el valor más alto y más bajo en esa ventana
            peaks.add(window.maxOrNull()?.toFloat() ?: 0f) // Pico máximo
            peaks.add(window.minOrNull()?.toFloat() ?: 0f) // Pico mínimo
        }

        // Normalizamos la lista de picos para que estén en el rango de -1.0 a 1.0
        val maxAbsValue = peaks.maxOfOrNull { kotlin.math.abs(it) }
        if (maxAbsValue == null || maxAbsValue == 0f) return peaks.map { 0f }

        return peaks.map { it / maxAbsValue }
    }
}