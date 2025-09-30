package com.android.harmoniatpi.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.android.harmoniatpi.data.database.entities.ProjectEntity
import com.android.harmoniatpi.data.database.entities.UserPreferencesEntity

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun guardarProject(projectEntity: ProjectEntity)
}