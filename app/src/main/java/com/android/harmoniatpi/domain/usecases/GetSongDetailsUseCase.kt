package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.SongRepository
import com.android.harmoniatpi.domain.model.song.SongDetails
import javax.inject.Inject

class GetSongDetailsUseCase @Inject constructor(
    private val songRepository: SongRepository
) {
    /**
     * Al usar 'operator fun invoke', podemos llamar a esta clase como si fuera una función.
     * Ejemplo: getSongDetailsUseCase(songId) en lugar de getSongDetailsUseCase.invoke(songId)
     */
    suspend operator fun invoke(songId: String): Result<SongDetails> {
        return songRepository.getSongDetails(songId)
    }
}