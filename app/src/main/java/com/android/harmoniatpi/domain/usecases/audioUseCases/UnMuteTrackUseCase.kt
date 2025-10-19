package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class UnMuteTrackUseCase @Inject constructor(private val mixer: AudioMixerRepository) {
    operator fun invoke(id: Long) = mixer.unMuteTrack(id)
}