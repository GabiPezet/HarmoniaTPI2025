package com.android.harmoniatpi.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.harmoniatpi.data.database.entities.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarProject(projectEntity: ProjectEntity)

    @Query("SELECT * FROM project ORDER BY id DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>
}

