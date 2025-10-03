package com.android.harmoniatpi.data.audio.record

import android.Manifest
import androidx.annotation.RequiresPermission
import com.android.harmoniatpi.domain.interfaces.AudioRecorder
import com.android.harmoniatpi.domain.interfaces.AudioRecorderRepository
import javax.inject.Inject

class AudioRecorderRepositoryImpl
@Inject constructor(private val recorder: AudioRecorder) : AudioRecorderRepository {

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording(outputFilePath: String): Result<Unit> {
        recorder.setOutputFile(outputFilePath)
        return recorder.startRecording()
    }

    override fun stopRecording() = recorder.stopRecording()
}