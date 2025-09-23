package com.android.harmoniatpi.data.audio.player

import com.android.harmoniatpi.domain.interfaces.AudioPlayerRepository
import java.io.File
import javax.inject.Inject

class AudioPlayerRepositoryImpl @Inject constructor(private val player: AudioPlayer) :
    AudioPlayerRepository {
    override fun playFile(inputFile: File): Result<Unit> = player.play(inputFile)

    override fun pause() = player.pause()

    override fun stop() = player.stop()
}