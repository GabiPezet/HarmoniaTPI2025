package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class UndoEffectUseCase @Inject constructor(
    private val mixer: AudioMixerRepository
) {
    operator fun invoke(id: Long): Result<Unit> {
        return mixer.undoEffect(id)
    }
}