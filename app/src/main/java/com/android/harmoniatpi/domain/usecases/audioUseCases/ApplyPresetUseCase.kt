package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.model.audio.PresetType
import javax.inject.Inject

class ApplyPresetUseCase @Inject constructor(
    private val repository: AudioMixerRepository
) {
    suspend operator fun invoke(trackId: Long, type: PresetType): Result<Unit> {
        return repository.applyPreset(trackId, type)
    }
}