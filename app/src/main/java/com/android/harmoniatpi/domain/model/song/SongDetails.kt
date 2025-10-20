package com.android.harmoniatpi.domain.model.song

/**
 * Esta clase representa los detalles de una canción, incluyendo su versión original y sus versiones derivadas.
 */
data class SongDetails(
    val originalSong: Song,
    val derivedVersions: List<DerivedVersion>

)
