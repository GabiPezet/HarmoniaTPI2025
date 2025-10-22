package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class StopPreviewUseCase @Inject constructor(
    private val audioMixerRepository: AudioMixerRepository
) {
    operator fun invoke() = audioMixerRepository.stopPreview()
}