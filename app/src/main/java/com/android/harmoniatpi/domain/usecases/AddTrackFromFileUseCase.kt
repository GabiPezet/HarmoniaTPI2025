package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class AddTrackFromFileUseCase @Inject constructor(
    private val mixer: AudioMixerRepository
) {
    /**
     * Crea una nueva pista importando el audio de un archivo local.
     *
     * @param sourceFilePath Ruta del archivo de audio de origen (ej. mp3, wav, pcm).
     * @return Result<Unit> indicando el éxito o fallo de la operación.
     */
    suspend operator fun invoke(sourceFilePath: String): Result<Unit> {
        return mixer.createTrackFromFile(sourceFilePath)
    }
}