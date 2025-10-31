package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import java.io.File
import java.io.FileNotFoundException
import javax.inject.Inject

class UploadAudioToStorageUseCase @Inject constructor(
    private val repository: Repository
) {
    /**
     * Sube un archivo de audio local a Firebase Storage.
     * @param projectId El ID del proyecto (para la ruta).
     * @param audioFile El archivo File local a subir.
     * @param fileName El nombre deseado para el archivo en Storage (ej. "mix.mp3", "track_0.mp3").
     * @return Result con la URL de descarga o una Excepción.
     */
    suspend operator fun invoke(projectId: String, audioFile: File, fileName: String): Result<String> {
        if (!audioFile.exists()) {
            return Result.failure(FileNotFoundException("El archivo de audio no existe: ${audioFile.path}"))
        }
        // Define la ruta en Storage: ej. "project_audio/projectId123/mix.mp3"
        val remotePath = "project_audio/$projectId/$fileName"
        return repository.uploadLocalFileToFirebaseStorage(audioFile.absolutePath, remotePath)
    }
}