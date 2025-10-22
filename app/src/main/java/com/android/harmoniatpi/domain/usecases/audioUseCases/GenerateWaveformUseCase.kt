package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.model.audio.WaveformResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
    private val BUFFER_SIZE = 64 * 1024
/*
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
*/
    operator fun invoke(path: String): WaveformResult = runBlocking(Dispatchers.Default) {
        val file = File(path)
        if (!file.exists() || file.length() == 0L) {
            return@runBlocking WaveformResult(emptyList(), 0L)
        }

        val totalSamples = file.length() / 2
        val durationMs = (totalSamples * 1000L) / 44100L

        // Un Channel actúa como una tubería entre el productor y el consumidor.
        val peaksChannel = Channel<FloatArray>(Channel.UNLIMITED)

        // Inicia el consumidor (normalizador) en una coroutine.
        // `async` se usa para que pueda devolver un resultado final (la lista normalizada).
        val normalizationJob = async {
            normalizePeaks(peaksChannel)
        }

        // Inicia el productor (generador de picos) en otra coroutine.
        launch {
            generatePeaks(file, peaksChannel)
        }

        // `await()` espera a que el consumidor termine su trabajo y devuelve la lista normalizada.
        val normalizedWaveform = normalizationJob.await()

        return@runBlocking WaveformResult(normalizedWaveform, durationMs)
    }
/*
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
*/
    /**
     * PRODUCTOR: Lee el archivo de audio por trozos y envía arrays de picos a través de un Channel.
     * Cuando termina, cierra el canal para señalar al consumidor que no hay más datos.
     */
    private suspend fun generatePeaks(file: File, channel: Channel<FloatArray>) {
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(BUFFER_SIZE)
            val window = ShortArray(SAMPLES_PER_PEAK)
            var windowIndex = 0

            // Un buffer temporal para acumular picos antes de enviarlos por el canal.
            val peaksBatch = mutableListOf<Float>()

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
                        peaksBatch.add(maxPeak.toFloat())
                        peaksBatch.add(minPeak.toFloat())
                        windowIndex = 0
                    }
                }
                // Envía el lote de picos y limpia la lista para el siguiente ciclo.
                if (peaksBatch.isNotEmpty()) {
                    channel.send(peaksBatch.toFloatArray())
                    peaksBatch.clear()
                }
            }
        }
        // Cierra el canal para indicar que la producción ha terminado.
        channel.close()
    }

    /**
     * CONSUMIDOR: Recibe lotes de picos desde un Channel, los almacena y calcula
     * el valor máximo absoluto en tiempo real para la normalización final.
     */
    private suspend fun normalizePeaks(channel: Channel<FloatArray>): List<Float> {
        val allPeaks = mutableListOf<Float>()
        var maxAbsValue = 1f

        // Itera sobre el canal hasta que se cierre.
        for (peaksBatch in channel) {
            for (peak in peaksBatch) {
                allPeaks.add(peak)
                val absPeak = abs(peak)
                if (absPeak > maxAbsValue) {
                    maxAbsValue = absPeak
                }
            }
        }

        if (allPeaks.isEmpty() || maxAbsValue == 0f) return emptyList()

        // Realiza la normalización final en una sola pasada.
        val normalizedPeaks = MutableList(allPeaks.size) { 0f }
        for (i in allPeaks.indices) {
            normalizedPeaks[i] = allPeaks[i] / maxAbsValue
        }
        return normalizedPeaks
    }
}