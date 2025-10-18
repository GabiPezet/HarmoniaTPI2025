package com.android.harmoniatpi.di

import com.android.harmoniatpi.domain.model.audio.AudioSourceType
import com.android.harmoniatpi.domain.model.audio.Track
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory

/**
 * Ayuda a crear un [Track]. Esto permite pasarle el directorio donde se guardarán las pistas dinámicamente
 * sin eliminar la inyección de AudioPlayer en el constructor.
 */
@AssistedFactory
interface TrackFactory {
    fun create(
        @Assisted("folder") folderPath: String,
        @Assisted("existing") existingFilePath: String? = null,
        @Assisted("id") idExist : Long? = null,
        @Assisted("sourceType") sourceType: AudioSourceType
    ): Track
}