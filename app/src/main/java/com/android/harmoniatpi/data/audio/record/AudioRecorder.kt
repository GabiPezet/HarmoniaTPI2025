package com.android.harmoniatpi.data.audio.record

import java.io.File

interface AudioRecorder {
    fun startRecording(outputFile: File): Result<Unit>
    fun stopRecording()
}