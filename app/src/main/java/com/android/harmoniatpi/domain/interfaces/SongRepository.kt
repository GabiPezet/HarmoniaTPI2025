package com.android.harmoniatpi.domain.interfaces

import com.android.harmoniatpi.domain.model.song.SongDetails

interface SongRepository {
    suspend fun getSongDetails(songId: String): Result<SongDetails>
}


