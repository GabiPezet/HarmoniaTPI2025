package com.android.harmoniatpi.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.harmoniatpi.data.database.entities.ProjectEntity

@Dao
interface ProjectDao {

    @Query("SELECT * FROM project")
    suspend fun getAllProjects(): List<ProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(project: ProjectEntity)

    @Query("DELETE FROM project WHERE id = :projectId")
    suspend fun deleteById(projectId: String)

    @Query("SELECT * FROM project WHERE id = :projectId LIMIT 1")
    suspend fun getProjectById(projectId: String): ProjectEntity?
}

