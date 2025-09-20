package com.android.harmoniatpi.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.android.harmoniatpi.data.database.entities.UserPreferencesEntity

@Dao
interface UserPreferencesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(userPreferences: UserPreferencesEntity)

    @Update
    suspend fun updateUserPreferences(userPreferences: UserPreferencesEntity)

    @Query("SELECT * FROM UserPreferencesTable WHERE userID = :userID LIMIT 1")
    suspend fun getUserPreferences(userID: String): UserPreferencesEntity?
}