package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioPlayerRepository
import java.io.File
import javax.inject.Inject

class StopAudioUseCase @Inject constructor(
    private val audioPlayerRepository: AudioPlayerRepository
) {
    operator fun invoke() = audioPlayerRepository.stop()
}