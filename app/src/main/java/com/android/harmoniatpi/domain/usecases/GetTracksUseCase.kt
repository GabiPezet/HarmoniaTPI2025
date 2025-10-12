package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class GetTracksUseCase @Inject constructor(private val mixer: AudioMixerRepository) {
    suspend operator fun invoke() = mixer.getTracks()
}