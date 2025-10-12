package com.android.harmoniatpi.di

import com.android.harmoniatpi.domain.model.audio.Track
import dagger.assisted.AssistedFactory

/**
 * Ayuda a crear un [Track]. Esto permite pasarle el directorio donde se guardarán las pistas dinámicamente
 * sin eliminar la inyección de AudioPlayer en el constructor.
 */
@AssistedFactory
interface TrackFactory {
    fun create(folderPath: String): Track
}