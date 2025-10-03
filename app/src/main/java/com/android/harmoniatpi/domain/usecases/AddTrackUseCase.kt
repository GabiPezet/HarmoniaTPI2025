package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class AddTrackUseCase @Inject constructor(private val mixer: AudioMixerRepository) {
    operator fun invoke() {
        mixer.createTrack()
    }
}