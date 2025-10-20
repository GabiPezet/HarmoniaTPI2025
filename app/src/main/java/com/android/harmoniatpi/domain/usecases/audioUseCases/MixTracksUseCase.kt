package com.android.harmoniatpi.domain.usecases.audioUseCases

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

class MixTracksUseCase @Inject constructor(@ApplicationContext private val context: Context) {
    operator fun invoke(projectId: String, trackPaths: List<String>): File? {
        try {
            val outputFile = File(context.filesDir, projectId.plus("_mix.pcm"))
            val files = trackPaths.map { File(it) }
            mixAudioFiles(files, outputFile)
            Log.d(TAG, "Mix finalizado. Archivo de salida: ${outputFile.absolutePath}")
            return outputFile
        } catch (e: Exception) {
            Log.e(TAG, "Error al mezclar las pistas", e)
            return null
        }
    }

    private fun mixAudioFiles(inputFiles: List<File>, outputFile: File) {
        // Leer todos los archivos como ByteArray
        val byteArrays = inputFiles.map { it.readBytes() }
        // Calcular longitud máxima en bytes
        val maxLength = byteArrays.maxOf { it.size }
        val outputBuffer = ByteArray(maxLength)

        var i = 0
        while (i < maxLength) {
            var mixedSample = 0

            inputFiles.indices.forEach { index ->
                val bytes = byteArrays[index]
                if (i + 1 < bytes.size) {
                    val sample = (bytes[i].toInt() and 0xFF) or (bytes[i + 1].toInt() shl 8)
                    mixedSample += sample
                }
            }

            // Normalizar para evitar clipping
            mixedSample = mixedSample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())

            // Escribir al buffer de salida (little endian)
            outputBuffer[i] = (mixedSample and 0xFF).toByte()
            outputBuffer[i + 1] = ((mixedSample shr 8) and 0xFF).toByte()

            i += 2
        }

        outputFile.writeBytes(outputBuffer)
    }

    private companion object {
        const val TAG = "MixTracksUseCase"

    }
}