package com.android.harmoniatpi.domain.interfaces

interface AudioPlayer {
    fun play(): Result<Unit>
    fun pause()
    fun stop()
    fun release()
    fun setFile(path: String)
    fun getFilePath(): String
    fun setOnPlaybackCompletedCallback(callback: () -> Unit)
}