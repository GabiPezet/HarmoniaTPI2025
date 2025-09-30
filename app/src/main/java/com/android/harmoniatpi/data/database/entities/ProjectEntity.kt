package com.android.harmoniatpi.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val hashtags: List<String>,
    val audioWaveform: List<Float>
)