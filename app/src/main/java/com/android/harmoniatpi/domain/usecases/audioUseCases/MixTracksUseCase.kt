package com.android.harmoniatpi.domain.usecases.audioUseCases

import android.content.Context
import android.util.Log
import com.android.harmoniatpi.domain.model.project.AudioTrack
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject

class MixTracksUseCase @Inject constructor(@ApplicationContext private val context: Context) {
    operator fun invoke(projectId: String, tracks: List<AudioTrack>): File? {
        try {
            if (tracks.isEmpty()) {
                Log.w(TAG, "No tracks provided for mixing project $projectId.")
                return null
            }

            val outputFile = File(context.filesDir, projectId.plus("_mix.pcm"))
            
            mixAudioFilesWithOffsets(tracks, outputFile)
            Log.d(TAG, "Mix finished. Output file: ${outputFile.absolutePath}")
            return outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Error mixing tracks for project $projectId", e)
            return null
        }
    }

    private fun mixAudioFilesWithOffsets(tracks: List<AudioTrack>, outputFile: File) {
        if (tracks.isEmpty()) {
            outputFile.writeBytes(byteArrayOf())
            return
        }

        val sampleRate = 44100
        val bytesPerSample = 2 
        var maxDurationMs = 0L
        tracks.forEach {
            val trackEndMs = it.startOffsetMs + it.durationMs
            if (trackEndMs > maxDurationMs) {
                maxDurationMs = trackEndMs
            }
        }

        val totalSamples = (maxDurationMs * sampleRate / 1000.0).toLong()
        val totalBytes = totalSamples * bytesPerSample
        val mixedSamples = IntArray(totalSamples.toInt()) { 0 }

        tracks.forEach { track ->
            val inputFile = File(track.path)
            if (inputFile.exists() && inputFile.length() > 0) {
                val startSampleOffset = (track.startOffsetMs * sampleRate / 1000.0).toLong()
                val startByteOffset = startSampleOffset * bytesPerSample

                try {
                    FileInputStream(inputFile).use { fis ->

                        val buffer = ByteArray(4096)
                        var bytesRead: Int
                        var currentSampleIndex = startSampleOffset.toInt()

                        while (fis.read(buffer).also { bytesRead = it } > 0 && currentSampleIndex < totalSamples) {
                            var i = 0
                            while (i < bytesRead && currentSampleIndex < totalSamples) {
                                if (i + 1 < bytesRead) {
                                    
                                    val low = buffer[i].toInt() and 0xFF
                                    val high = buffer[i + 1].toInt() 
                                    val sample = (high shl 8) or low

                                    
                                    mixedSamples[currentSampleIndex] += sample

                                    currentSampleIndex++
                                }
                                i += bytesPerSample 
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading track ${track.id} (${track.path})", e)
                }
            } else {
                Log.w(TAG, "Skipping non-existent or empty track: ${track.path}")
            }
        }

        val outputBytes = ByteArray(totalBytes.toInt())
        for (i in mixedSamples.indices) {

            val clippedSample = mixedSamples[i].coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            outputBytes[i * 2] = (clippedSample and 0xFF).toByte()
            outputBytes[i * 2 + 1] = ((clippedSample shr 8) and 0xFF).toByte()
        }

        try {
            outputFile.writeBytes(outputBytes)
            
            if (outputFile.exists() && outputFile.length() > 0) {
                Log.i(TAG, "mixAudioFilesWithOffsets - Archivo de mezcla generado OK: ${outputFile.absolutePath}, Tamaño: ${outputFile.length()} bytes")
            } else {
                Log.e(TAG, "mixAudioFilesWithOffsets - ¡ERROR! Archivo de mezcla NO generado o vacío: ${outputFile.absolutePath}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "mixAudioFilesWithOffsets - Error escribiendo archivo de mezcla ${outputFile.absolutePath}", e)
            if(outputFile.exists()) outputFile.delete() 
            throw e 
        }
    }

    private companion object {
        const val TAG = "MixTracksUseCase"

    }
}