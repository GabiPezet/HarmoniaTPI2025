package com.android.harmoniatpi.data.audio.util

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import kotlin.math.PI
import kotlin.math.sin

class TremoloProcessor(
    private val frequency: Float,
    private val depth: Float,
    private val sampleRate: Float
) : AudioProcessor {
    private var sampleCount = 0L

    override fun process(audioEvent: AudioEvent): Boolean {
        val buffer = audioEvent.floatBuffer
        for (i in buffer.indices) {
            // LFO (Low Frequency Oscillator)
            val time = sampleCount.toDouble() / sampleRate
            val lfo = sin(2.0 * PI * frequency * time)

            // Modulamos la amplitud
            // depth controla qué tanto baja el volumen
            val gain = 1.0 - (depth * (0.5 * (1.0 + lfo)))

            buffer[i] *= gain.toFloat()
            sampleCount++
        }
        return true
    }
    override fun processingFinished() {}
}