package com.android.harmoniatpi.data.database

import com.android.harmoniatpi.data.database.entities.ProjectEntity
import com.android.harmoniatpi.domain.model.project.Project


fun ProjectEntity.toDomain(): Project = Project(
    title = title,
    description = description,
    hashtags = hashtags.split(",").map { it.trim() },
    audioWaveform = emptyList()
)

fun Project.toEntity(): ProjectEntity = ProjectEntity(
    title = title,
    description = description,
    hashtags = hashtags.joinToString(",")
)