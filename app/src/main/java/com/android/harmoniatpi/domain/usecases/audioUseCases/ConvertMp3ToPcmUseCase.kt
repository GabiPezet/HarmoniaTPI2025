package com.android.harmoniatpi.domain.usecases.audioUseCases

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.harmoniatpi.data.audio.util.AudioConverter
import java.io.File
import javax.inject.Inject

class ConvertMp3ToPcmUseCase @Inject constructor(
    private val audioConverter: AudioConverter
) {
    /**
     * Convierte un archivo MP3 de origen a un archivo PCM de destino.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    suspend operator fun invoke(mp3File: File, pcmFile: File): Result<Unit> {
        // Tu AudioConverter espera una Uri, no un File.
        val inputUri = Uri.fromFile(mp3File)

        return audioConverter.convertToPcm(inputUri, pcmFile)
    }
}