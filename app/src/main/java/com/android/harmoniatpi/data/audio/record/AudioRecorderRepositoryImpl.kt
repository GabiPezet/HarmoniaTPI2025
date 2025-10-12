package com.android.harmoniatpi.data.audio.record

import android.Manifest
import androidx.annotation.RequiresPermission
import com.android.harmoniatpi.domain.interfaces.AudioRecorderRepository
import java.io.File
import javax.inject.Inject

class AudioRecorderRepositoryImpl
@Inject constructor(private val recorder: AudioRecorder) : AudioRecorderRepository {

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording(outputFile: File): Result<Unit> =
        recorder.startRecording(outputFile)

    override fun stopRecording() = recorder.stopRecording()
}