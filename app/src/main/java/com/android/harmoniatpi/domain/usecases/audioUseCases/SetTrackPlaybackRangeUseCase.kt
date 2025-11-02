package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class SetTrackPlaybackRangeUseCase @Inject constructor(
    private val repository: AudioMixerRepository
) {
    operator fun invoke(trackId: Long, startMs: Long, endMs: Long, totalDurationMs: Long) =
        repository.setPlaybackRange(trackId, startMs, endMs, totalDurationMs)
}