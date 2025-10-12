package com.android.harmoniatpi.data.audio.player

import java.io.File

interface AudioPlayer {
    fun play(file: File): Result<Unit>
    fun pause()
    fun stop()
    fun setOnPlaybackCompletedCallback(callback: () -> Unit)
}