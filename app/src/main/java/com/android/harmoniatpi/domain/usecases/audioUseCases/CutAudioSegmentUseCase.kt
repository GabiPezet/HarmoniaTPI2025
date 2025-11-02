package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class CutAudioSegmentUseCase @Inject constructor(
    private val repository: AudioMixerRepository
) {
    operator fun invoke(id: Long, startMs: Long, endMs: Long): Result<Unit> {
        return repository.cutAudioSegment(id, startMs, endMs)
    }
}