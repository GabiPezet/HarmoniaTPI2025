package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class SetTrackOffsetUseCase @Inject constructor(
    private val mixer: AudioMixerRepository
) {
    operator fun invoke(id: Long, offsetMs: Long) {
        mixer.setTrackOffset(id, offsetMs)
    }
}