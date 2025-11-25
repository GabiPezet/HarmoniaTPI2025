package com.android.harmoniatpi.data.audio.util

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor

class DistortionProcessor(private val threshold: Double) : AudioProcessor {
    override fun process(audioEvent: AudioEvent): Boolean {
        val buffer = audioEvent.floatBuffer
        for (i in buffer.indices) {
            // Hard Clipping simple
            if (buffer[i] > threshold) {
                buffer[i] = threshold.toFloat()
            } else if (buffer[i] < -threshold) {
                buffer[i] = -threshold.toFloat()
            }
            // Opcional: Compensar volumen (Make-up gain)
            buffer[i] /= threshold.toFloat()
        }
        return true
    }
    override fun processingFinished() {}
}