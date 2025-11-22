package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class ApplyHighPassFilterUseCase @Inject constructor(
    private val repository: AudioMixerRepository
) {
    suspend operator fun invoke(trackId: Long, frequency: Float): Result<Unit> {
        return repository.applyHighPassFilter(trackId, frequency)
    }
}