package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class MuteTrackUseCase @Inject constructor(private val mixer: AudioMixerRepository) {
    operator fun invoke(id: Long) = mixer.muteTrack(id)
}