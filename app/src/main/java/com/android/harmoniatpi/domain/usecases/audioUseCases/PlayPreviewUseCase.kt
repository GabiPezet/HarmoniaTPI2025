package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class PlayPreviewUseCase @Inject constructor(
    private val audioMixerRepository: AudioMixerRepository
) {
    operator fun invoke(filePath: String) = audioMixerRepository.playPreview(filePath)
}