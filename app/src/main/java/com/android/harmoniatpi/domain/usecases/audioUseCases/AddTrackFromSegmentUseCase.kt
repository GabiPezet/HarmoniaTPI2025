package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class AddTrackFromSegmentUseCase @Inject constructor(
    private val repository: AudioMixerRepository
) {
    suspend operator fun invoke(sourcePath: String, startMs: Long, endMs: Long): Result<Unit> {
        return repository.addTrackFromSegment(sourcePath, startMs, endMs)
    }
}