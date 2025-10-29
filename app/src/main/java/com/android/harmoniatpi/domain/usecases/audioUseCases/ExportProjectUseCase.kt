package com.android.harmoniatpi.domain.usecases.audioUseCases

import android.content.Context
import android.util.Log
import com.android.harmoniatpi.data.audio.util.AudioConverter
import com.android.harmoniatpi.domain.model.project.AudioTrack
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


    suspend operator fun invoke(projectId: String, tracks: List<AudioTrack>): Result<ExportResult> =
        withContext(Dispatchers.IO) {

            if (tracks.isEmpty()) {
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


                if (tracks.size > 1) {
                    Log.d(TAG, "Mezclando ${tracks.size} pistas para $projectId...")

                    mixedPcmFile = mixTracksUseCase(projectId, tracks)
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

                tracks.forEachIndexed { index, audioTrack ->
                    val pcmFile = File(audioTrack.path)
                    if (!pcmFile.exists() || pcmFile.length() == 0L) {
                        Log.w(TAG, "Omitiendo pista vacía o inexistente: ${audioTrack.path}")
                        return@forEachIndexed
                    }


                    val trackId = audioTrack.id
                    val individualMp3OutputPath =
                        File(outputDir, "${projectId}_track_${trackId}.mp3").absolutePath
                    val individualMp3File = File(individualMp3OutputPath)

                    Log.d(
                        TAG,
                        "Convirtiendo pista ${audioTrack.path} a MP3: $individualMp3OutputPath"
                    )
                    audioConverter.convertPcmToMp3(pcmFile, individualMp3File)
                        .onSuccess {
                            individualMp3Files.add(individualMp3File)
                            Log.i(TAG, "Pista individual ${pcmFile.name} convertida a MP3.")
                        }
                        .onFailure {
                            Log.e(TAG, "Fallo al convertir pista individual ${audioTrack.path}", it)
                            if (individualMp3File.exists()) individualMp3File.delete()
                        }
                }


                if (tracks.size == 1 && individualMp3Files.isNotEmpty()) {
                    mixedMp3File = individualMp3Files.first()
                }


                if (mixedMp3File == null && individualMp3Files.isEmpty()) {
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
                        if (it.delete()) {
                            Log.d(TAG, "Archivo temporal ${it.name} eliminado.")
                        } else {
                            Log.w(TAG, "No se pudo eliminar el archivo temporal ${it.name}")
                        }
                    }
                }
            }
        }
}