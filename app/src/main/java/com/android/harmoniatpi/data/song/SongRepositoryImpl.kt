package com.android.harmoniatpi.data.song

import com.android.harmoniatpi.domain.interfaces.SongRepository
import com.android.harmoniatpi.domain.model.song.DerivedVersion
import com.android.harmoniatpi.domain.model.song.Song
import com.android.harmoniatpi.domain.model.song.SongDetails
import com.android.harmoniatpi.domain.model.song.VersionType
import com.android.harmoniatpi.domain.model.user.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementación del repositorio de canciones.
 */
class SongRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : SongRepository {
    /**
     * Obtiene los detalles de una canción, incluyendo su versión original y sus versiones derivadas.
     */
    override suspend fun getSongDetails(proyectId: String): Result<SongDetails> {
        return try {
            // 1. Obtener el documento de la canción original
            val songDocument = firestore.collection("songs").document(proyectId).get().await()
            if (!songDocument.exists()) {
                throw Exception("La canción con ID $proyectId no fue encontrada.")
            }

            // 2. Obtener el creador de la canción original a partir de su ID
            val creatorId = songDocument.getString("creatorId") ?: throw Exception("Falta el ID del creador.")
            val creatorUser = getUserProfile(creatorId)

            // 3. Mapear el documento a nuestro modelo de dominio 'Song'
            val originalSong = Song(
                id = songDocument.id,
                title = songDocument.getString("title") ?: "Sin Título",
                imageUrl = songDocument.getString("imageUrl"),
                audioUrl = songDocument.getString("audioUrl") ?: "",
                durationMillis = songDocument.getLong("durationMillis") ?: 0L,
                projectId = songDocument.getString("projectId"),
                creator = creatorUser, // <- Usamos el objeto User completo
                versionType = VersionType.ORIGINAL
            )

            // 4. Obtener la colección de versiones derivadas
            val versionsSnapshot = songDocument.reference.collection("derivedVersions").get().await()

            // Usamos mapNotNull para descartar cualquier versión que tenga datos corruptos
            val derivedVersions = versionsSnapshot.documents.mapNotNull { versionDoc ->
                try {
                    val versionCreatorId = versionDoc.getString("creatorId") ?: return@mapNotNull null
                    val versionCreator = getUserProfile(versionCreatorId)
                    DerivedVersion(
                        id = versionDoc.id,
                        creator = versionCreator,
                        projectId = versionDoc.getString("projectId"),
                        audioUrl = versionDoc.getString("audioUrl"),
                        durationMillis = versionDoc.getLong("durationMillis") ?: 0L
                    )
                } catch (e: Exception) {
                    // Si un perfil de usuario no se encuentra, omitimos esa versión derivada
                    null
                }
            }

            // 5. Ensamblar y devolver el objeto 'SongDetails'
            val songDetails = SongDetails(
                originalSong = originalSong,
                derivedVersions = derivedVersions
            )
            Result.success(songDetails)

        } catch (e: Exception) {
            // Si algo falla en el proceso principal, devolvemos un error
            Result.failure(e)
        }
    }

    /**
     * Función de ayuda para obtener el perfil público de un usuario desde la colección 'users'.
     * Esto evita la duplicación de código y centraliza la lógica.
     */
    private suspend fun getUserProfile(userId: String): User {
        val userDocument = firestore.collection("users").document(userId).get().await()
        if (!userDocument.exists()) {
            throw Exception("Usuario con ID $userId no encontrado.")
        }
        return User(
            id = userDocument.id,
            name = userDocument.getString("name") ?: "Usuario Desconocido",
            avatarUrl = userDocument.getString("avatarUrl")
        )
    }
}