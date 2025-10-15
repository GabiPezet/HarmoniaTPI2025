package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetCurrentPlaybackPositionUseCase @Inject constructor(
    private val mixer: AudioMixerRepository
) {
    suspend operator fun invoke(): StateFlow<Long> = mixer.getCurrentPlaybackPosition()
}