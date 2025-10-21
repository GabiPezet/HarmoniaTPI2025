package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.model.audio.WaveformResult
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import kotlin.math.abs

class GenerateWaveformUseCase @Inject constructor() {

    // --- CONFIGURATION CONSTANTS ---
    // How many raw PCM samples are processed to produce one min/max peak pair.
    // A larger number means a faster but less detailed waveform. 256 is a good default.
    private val SAMPLES_PER_PEAK = 256

    // The size of the buffer to read from the file at once (in bytes).
    // This should be a multiple of (SAMPLES_PER_PEAK * 2 bytes/sample * 2 for min/max).
    // 8KB is a robust and efficient size.
    private val BUFFER_SIZE = 8 * 1024

    operator fun invoke(path: String): WaveformResult {
        val file = File(path)
        if (!file.exists() || file.length() == 0L) {
            return WaveformResult(emptyList(), 0L)
        }

        // The total number of samples is needed for the duration calculation.
        val totalSamples = file.length() / 2 // 2 bytes per sample in 16-bit PCM
        val durationMs = (totalSamples * 1000L) / 44100L

        val peaks = generatePeaks(file)
        val normalizedWaveform = normalizePeaks(peaks)

        return WaveformResult(normalizedWaveform, durationMs)
    }

    private fun generatePeaks(file: File): List<Float> {
        val peaks = mutableListOf<Float>()

        // Use a FileInputStream to process the file as a stream.
        FileInputStream(file).use { fis ->
            // Use a reusable buffer to read chunks of the file.
            val buffer = ByteArray(BUFFER_SIZE)

            // This will hold the samples for one window to find a single min/max pair.
            val window = ShortArray(SAMPLES_PER_PEAK)
            var windowIndex = 0

            // Read from the file until the end is reached.
            while (fis.read(buffer) != -1) {
                // Wrap the byte buffer to read shorts directly (much faster).
                val shortBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()

                while (shortBuffer.hasRemaining()) {
                    window[windowIndex++] = shortBuffer.get()

                    // When our window is full, process it.
                    if (windowIndex == SAMPLES_PER_PEAK) {
                        var minPeak: Short = Short.MAX_VALUE
                        var maxPeak: Short = Short.MIN_VALUE

                        // Find min/max in the window with a single, efficient loop.
                        for (sample in window) {
                            if (sample > maxPeak) maxPeak = sample
                            if (sample < minPeak) minPeak = sample
                        }

                        // Add the found peaks to our list.
                        peaks.add(maxPeak.toFloat())
                        peaks.add(minPeak.toFloat())

                        // Reset the window index for the next set of samples.
                        windowIndex = 0
                    }
                }
            }
        }
        return peaks
    }

    private fun normalizePeaks(peaks: List<Float>): List<Float> {
        if (peaks.isEmpty()) return emptyList()

        // Find the absolute maximum peak value in the entire list.
        val maxAbsValue = peaks.maxOfOrNull { abs(it) } ?: 1f

        if (maxAbsValue == 0f) return peaks.map { 0f }

        // Normalize all peaks by this maximum value.
        // This is faster than creating a new list with .map if peaks is a MutableList.
        val normalizedPeaks = MutableList(peaks.size) { 0f }
        for (i in peaks.indices) {
            normalizedPeaks[i] = peaks[i] / maxAbsValue
        }
        return normalizedPeaks
    }
}