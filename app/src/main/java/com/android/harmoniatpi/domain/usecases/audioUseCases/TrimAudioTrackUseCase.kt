package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class TrimAudioTrackUseCase @Inject constructor(
    private val mixer: AudioMixerRepository
) {

    operator fun invoke(id: Long, startMs: Long, endMs: Long): Result<Unit> {
        return mixer.trimTrack(id, startMs, endMs)
    }


}