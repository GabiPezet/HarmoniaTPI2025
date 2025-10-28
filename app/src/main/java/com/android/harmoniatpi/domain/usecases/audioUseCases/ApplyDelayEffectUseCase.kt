package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class ApplyDelayEffectUseCase @Inject constructor(
    private val repository: AudioMixerRepository
) {
    suspend operator fun invoke(trackId: Long, delayTimeInSeconds: Float, decay: Float): Result<Unit> {
        return repository.applyDelayEffect(trackId, delayTimeInSeconds, decay)
    }
}