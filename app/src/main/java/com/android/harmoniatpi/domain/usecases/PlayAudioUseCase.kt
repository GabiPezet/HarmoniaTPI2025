package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioPlayerRepository
import java.io.File
import javax.inject.Inject

class PlayAudioUseCase @Inject constructor(
    private val audioPlayerRepository: AudioPlayerRepository
) {
    operator fun invoke(inputFile: File) = audioPlayerRepository.playFile(inputFile)
}