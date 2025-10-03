package com.android.harmoniatpi.di

import com.android.harmoniatpi.domain.model.audio.Track
import dagger.assisted.AssistedFactory

@AssistedFactory
interface TrackFactory {
    fun create(folderPath: String): Track
}