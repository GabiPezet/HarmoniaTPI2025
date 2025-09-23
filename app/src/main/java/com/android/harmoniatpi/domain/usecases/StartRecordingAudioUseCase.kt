package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioRecorderRepository
import java.io.File
import javax.inject.Inject

class StartRecordingAudioUseCase @Inject constructor(
    private val audioRecorderRepository: AudioRecorderRepository
) {
    operator fun invoke(outputFile: File) = audioRecorderRepository.startRecording(outputFile)
}