package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class ApplyFlangerEffectUseCase @Inject constructor(
    private val repository: AudioMixerRepository
) {
    suspend operator fun invoke(trackId: Long, rate: Float, wet: Float): Result<Unit> {
        return repository.applyFlangerEffect(trackId, rate, wet)
    }
}