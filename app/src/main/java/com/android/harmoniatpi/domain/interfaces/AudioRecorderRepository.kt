package com.android.harmoniatpi.domain.interfaces

interface AudioRecorderRepository {
    fun startRecording(outputFilePath: String): Result<Unit>
    fun stopRecording(): Result<Unit>
}