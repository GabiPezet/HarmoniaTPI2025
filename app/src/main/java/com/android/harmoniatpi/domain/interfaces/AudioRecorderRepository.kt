package com.android.harmoniatpi.domain.interfaces

import java.io.File

interface AudioRecorderRepository {
    fun startRecording(outputFile: File): Result<Unit>
    fun stopRecording()
}