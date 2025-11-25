package com.android.harmoniatpi.data.audio.util

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor

class FadeOutProcessor(
    private val durationSec: Float,
    private val totalLengthSec: Float,
    private val sampleRate: Int
) : AudioProcessor {

    private var frame = 0
    private val totalSamples = (totalLengthSec * sampleRate).toInt()

    override fun process(buffer: AudioEvent): Boolean {
        val fadeSamples = (durationSec * sampleRate).toInt()
        val startFade = totalSamples - fadeSamples

        val audio = buffer.floatBuffer
        val globalFrame = frame

        for (i in audio.indices) {
            val absolutePos = globalFrame + i
            val gain =
                if (absolutePos > startFade)
                    1f - ((absolutePos - startFade).toFloat() / fadeSamples).coerceIn(0f, 1f)
                else 1f

            audio[i] *= gain
        }

        frame += audio.size
        return true
    }

    override fun processingFinished() {}
}