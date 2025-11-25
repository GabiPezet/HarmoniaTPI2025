package com.android.harmoniatpi.data.audio.util

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor

class FadeInProcessor(
    private val durationSec: Float,
    private val sampleRate: Int
) : AudioProcessor {

    private var frame = 0

    override fun process(buffer: AudioEvent): Boolean {
        val fadeSamples = (durationSec * sampleRate).toInt()
        val audio = buffer.floatBuffer

        for (i in audio.indices) {
            val gain = (frame.toFloat() / fadeSamples).coerceIn(0f, 1f)
            audio[i] *= gain
            frame++
        }

        return true
    }

    override fun processingFinished() {}
}