package com.android.harmoniatpi.domain.model.song

data class SongDetails(
    val originalSong: Song,
    val derivedVersions: List<DerivedVersion>

)
