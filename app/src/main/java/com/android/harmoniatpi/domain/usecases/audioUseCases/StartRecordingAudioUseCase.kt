package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioRecorderRepository
import javax.inject.Inject

class StartRecordingAudioUseCase @Inject constructor(
    private val audioRecorderRepository: AudioRecorderRepository
) {
    operator fun invoke(outputFileName: String, audioSource: Int) =
        audioRecorderRepository.startRecording(outputFileName, audioSource)
}