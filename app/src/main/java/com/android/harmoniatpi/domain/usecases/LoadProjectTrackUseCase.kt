package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

class LoadProjectTrackUseCase @Inject constructor(
    private val mixer: AudioMixerRepository
) {
    /**
     * Carga una pista PCM existente en el motor de audio (AudioMixerRepository)
     * sin volver a convertirla.
     *
     * @param pcmFilePath Ruta absoluta del archivo PCM.
     * @return Result<Unit> indicando el éxito o fallo de la operación.
     */
    suspend operator fun invoke(pcmFilePath: String, id: Long, sourceType: AudioSourceType): Result<Unit> {
        return try {
            val file = File(pcmFilePath)
            if (!file.exists()) {
                return Result.failure(FileNotFoundException("Archivo PCM no encontrado: $pcmFilePath"))
            }

            mixer.loadPcmTrack(file, id,  sourceType)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearAllTracks() = mixer.clearAllTracks()
}
