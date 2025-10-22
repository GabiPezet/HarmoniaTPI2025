package com.android.harmoniatpi.domain.usecases.audioUseCases

import android.content.Context
import android.util.Log
import com.android.harmoniatpi.data.audio.util.AudioConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

data class ExportResult(
    val mixedMp3: File?,
    val individualMp3s: List<File>
)

class ExportProjectUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mixTracksUseCase: MixTracksUseCase,
    private val audioConverter: AudioConverter
) {
    private val TAG = "ExportProjectUseCase"

    suspend operator fun invoke(projectId: String, trackPaths: List<String>): Result<ExportResult> = withContext(
        Dispatchers.IO) {
        if (trackPaths.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("No hay pistas para exportar."))
        }

        var mixedPcmFile: File? = null
        var mixedMp3File: File? = null
        val individualMp3Files = mutableListOf<File>()
        val filesToDelete = mutableListOf<File>()

        try {
            val outputDir = context.getExternalFilesDir("exported_music/$projectId")
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs()
            }
            if (outputDir == null) {
                return@withContext Result.failure(IOException("No se pudo crear el directorio de salida."))
            }

            if (trackPaths.size > 1) {
                Log.d(TAG, "Mezclando ${trackPaths.size} pistas para $projectId...")
                mixedPcmFile = mixTracksUseCase(projectId, trackPaths)
                if (mixedPcmFile == null) {
                    throw IOException("Fallo al mezclar las pistas a PCM.")
                }
                filesToDelete.add(mixedPcmFile)

                val mixedMp3OutputPath = File(outputDir, "${projectId}_mix.mp3").absolutePath
                Log.d(TAG, "Convirtiendo mezcla PCM a MP3: $mixedMp3OutputPath")
                audioConverter.convertPcmToMp3(mixedPcmFile, File(mixedMp3OutputPath))
                    .onSuccess {
                        mixedMp3File = File(mixedMp3OutputPath)
                        Log.i(TAG, "Mezcla convertida a MP3 exitosamente.")
                    }
                    .onFailure { throw it }
            }

            Log.d(TAG, "Convirtiendo pistas individuales a MP3...")
            trackPaths.forEachIndexed { index, pcmPath ->
                val pcmFile = File(pcmPath)
                if (!pcmFile.exists() || pcmFile.length() == 0L) {
                    Log.w(TAG, "Omitiendo pista vacía o inexistente: $pcmPath")
                    return@forEachIndexed
                }

                val trackId = pcmFile.nameWithoutExtension
                val individualMp3OutputPath = File(outputDir, "${projectId}_track_${trackId}.mp3").absolutePath
                val individualMp3File = File(individualMp3OutputPath)

                Log.d(TAG, "Convirtiendo pista $pcmPath a MP3: $individualMp3OutputPath")
                audioConverter.convertPcmToMp3(pcmFile, individualMp3File)
                    .onSuccess {
                        individualMp3Files.add(individualMp3File)
                        Log.i(TAG, "Pista individual ${pcmFile.name} convertida a MP3.")
                    }
                    .onFailure {
                        Log.e(TAG, "Fallo al convertir pista individual $pcmPath", it)
                        // Decidir si continuar o fallar todo el proceso
                        // Por ahora, solo logueamos y continuamos
                        if (individualMp3File.exists()) individualMp3File.delete()
                    }
            }

            if (trackPaths.size == 1 && individualMp3Files.isNotEmpty()) {
                mixedMp3File = individualMp3Files.first()
            }

            if (mixedMp3File == null && individualMp3Files.isEmpty()){
                throw IOException("No se pudo generar ningún archivo MP3.")
            }


            Result.success(ExportResult(mixedMp3File, individualMp3Files))

        } catch (e: Exception) {
            Log.e(TAG, "Error durante la exportación del proyecto $projectId", e)
            mixedMp3File?.delete()
            individualMp3Files.forEach { it.delete() }
            Result.failure(e)
        } finally {
            filesToDelete.forEach {
                if (it.exists()) {
                    if(it.delete()){
                        Log.d(TAG,"Archivo temporal ${it.name} eliminado.")
                    } else {
                        Log.w(TAG, "No se pudo eliminar el archivo temporal ${it.name}")
                    }
                }
            }
        }
    }
}