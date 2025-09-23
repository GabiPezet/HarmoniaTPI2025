package com.android.harmoniatpi.domain.interfaces

import java.io.File

interface AudioPlayerRepository {
    fun playFile(inputFile: File): Result<Unit>
    fun pause()
    fun stop()
}