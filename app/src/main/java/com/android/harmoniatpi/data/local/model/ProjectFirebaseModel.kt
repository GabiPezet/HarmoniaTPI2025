package com.android.harmoniatpi.data.local.model

import com.android.harmoniatpi.data.database.entities.ProjectEntity
import com.android.harmoniatpi.di.util.JsonUtils
import com.android.harmoniatpi.domain.model.project.Project

data class ProjectFirebaseModel(
    val id: String = "",
    val ownerId: String = "",
    val name: String = "",
    val lastName: String = "",
    val title: String = "",
    val description: String = "",
    val duration: Long = 0L,
    val createdAt: String = "",
    // --- Campos como JSON String ---
    val hashtags: String = "",        // ✨ TIPO STRING
    val forkedByUserIds: String = "", // ✨ TIPO STRING
    // --- URLs / JSON ---
    val publishedAudioUrl: String? = null,
    val publishedTrackUrls: String = "", // ✨ TIPO STRING
    // --- Otros campos ---
    val likes: Int = 0,
    val totalShared: Int = 0,
    val isPublished: Boolean = true
) {
    // Constructor sin argumentos (generado por Kotlin)

    /**
     * Convierte este modelo de Firestore al modelo Entity de Room.
     * Pasa los Strings JSON directamente.
     */
    fun toEntity(): ProjectEntity {
        return ProjectEntity(
            id = this.id,
            ownerId = this.ownerId,
            name = this.name,
            lastName = this.lastName,
            title = this.title,
            description = this.description,
            duration = this.duration.toString(),
            createdAt = this.createdAt,
            status = true,
            likes = this.likes,
            totalShared = this.totalShared,
            comments = "[]",
            urlCompleteAudio = this.publishedAudioUrl,
            urlAudioTracks = this.publishedTrackUrls, // Pasa String JSON
            hashtags = this.hashtags,                 // Pasa String JSON
            forkedByUserIds = this.forkedByUserIds, // Pasa String JSON
            originalProjectId = null,
            isPublished = this.isPublished
        )
    }

    /**
     * Convierte este modelo de Firestore al modelo de dominio 'Project'.
     * (Función auxiliar que usa la lógica Entity -> Domain)
     */
    fun toDomain(jsonUtils: JsonUtils): Project {
        // Sigue el flujo Firebase -> Entity -> Domain
        return this.toEntity().toDomain(jsonUtils)
    }
}
// Función de extensión para convertir tu modelo de dominio a FirebaseModel