package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class OnPreviewCompletedUseCase @Inject constructor(
    private val audioMixerRepository: AudioMixerRepository
) {
    operator fun invoke(): SharedFlow<Unit> = audioMixerRepository.onPreviewCompleted()
}