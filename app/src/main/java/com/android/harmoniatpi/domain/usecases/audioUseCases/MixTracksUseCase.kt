package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import javax.inject.Inject

class MixTracksUseCase @Inject constructor(private val mixer: AudioMixerRepository) {
    operator fun invoke(projectId: String) {
        val outputFile = mixer.mixTracks(projectId.plus("_mix.pcm"))
        //Usar el archivo de salida generado luego de mezclar las pistas
    }
}