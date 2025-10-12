package com.android.harmoniatpi.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.harmoniatpi.domain.model.project.Project

@Entity(tableName = "project")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val hashtags: String
)