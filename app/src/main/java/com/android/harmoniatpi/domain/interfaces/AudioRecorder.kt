package com.android.harmoniatpi.domain.interfaces

interface AudioRecorder {
    fun setOutputFile(path: String)
    fun startRecording(): Result<Unit>
    fun stopRecording(): Result<Unit>
}