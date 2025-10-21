package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class UndoTrimUseCase @Inject constructor(
    private val mixer: AudioMixerRepository
) {
    /**
     * Deshace el último recorte realizado en la pista.
     *
     * @param id El ID de la pista.
     * @return Result<Unit> indicando el éxito o fallo de la operación.
     */
    operator fun invoke(id: Long): Result<Unit> {
        return mixer.undoTrim(id)
    }
}