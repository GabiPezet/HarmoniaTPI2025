package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import javax.inject.Inject

class AddTrackUseCase @Inject constructor(private val mixer: AudioMixerRepository) {
    operator fun invoke(sourceType: AudioSourceType) {
        mixer.createTrack(sourceType)
    }
}