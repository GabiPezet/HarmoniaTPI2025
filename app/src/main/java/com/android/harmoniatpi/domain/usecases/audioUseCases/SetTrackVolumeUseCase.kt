package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class SetTrackVolumeUseCase @Inject constructor(private val mixer: AudioMixerRepository) {
    operator fun invoke(id: Long, volume: Float) = mixer.setTrackVolume(id, volume)
}