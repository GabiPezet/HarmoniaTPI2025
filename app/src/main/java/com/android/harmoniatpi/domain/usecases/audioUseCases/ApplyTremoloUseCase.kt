package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class ApplyTremoloUseCase @Inject constructor(
    private val repository: AudioMixerRepository
) {
    suspend operator fun invoke(trackId: Long, frequency: Float, depth: Float): Result<Unit> {
        return repository.applyTremolo(trackId, frequency, depth)
    }
}