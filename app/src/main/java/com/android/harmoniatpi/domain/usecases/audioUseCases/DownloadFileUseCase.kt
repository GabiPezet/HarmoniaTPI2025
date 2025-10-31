package com.android.harmoniatpi.domain.usecases.audioUseCases

import android.util.Log
import java.io.File
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadFileUseCase @Inject constructor() {
    /**
     * Descarga un archivo desde una URL y lo guarda en un archivo de destino.
     */
    suspend operator fun invoke(remoteUrl: String, destinationFile: File): Result<File> = withContext(Dispatchers.IO) {
        Log.d("DownloadFileUseCase", "Descargando $remoteUrl hacia ${destinationFile.absolutePath}")
        try {
            // Asegúrate de que el directorio padre exista
            destinationFile.parentFile?.mkdirs()

            URL(remoteUrl).openStream().use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            Log.d("DownloadFileUseCase", "Descarga completada.")
            Result.success(destinationFile)
        } catch (e: Exception) {
            Log.e("DownloadFileUseCase", "Error en la descarga", e)
            Result.failure(e)
        }
    }
}