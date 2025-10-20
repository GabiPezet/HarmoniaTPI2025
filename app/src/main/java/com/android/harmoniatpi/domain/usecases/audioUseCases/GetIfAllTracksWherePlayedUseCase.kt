package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class GetIfAllTracksWherePlayedUseCase @Inject constructor(private val mixer: AudioMixerRepository) {
    suspend operator fun invoke() = mixer.allTracksWerePlayed()
}