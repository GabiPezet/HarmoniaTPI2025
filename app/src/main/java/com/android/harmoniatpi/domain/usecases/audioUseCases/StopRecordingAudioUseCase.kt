package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioRecorderRepository
import javax.inject.Inject

class StopRecordingAudioUseCase @Inject constructor(
    private val audioRecorderRepository: AudioRecorderRepository
) {
    operator fun invoke() = audioRecorderRepository.stopRecording()
}