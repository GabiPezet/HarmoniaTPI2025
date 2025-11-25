package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class ApplyDistortionUseCase @Inject constructor(
    private val repository: AudioMixerRepository
) {
    suspend operator fun invoke(trackId: Long, drive: Float): Result<Unit> {
        return repository.applyDistortion(trackId, drive)
    }
}