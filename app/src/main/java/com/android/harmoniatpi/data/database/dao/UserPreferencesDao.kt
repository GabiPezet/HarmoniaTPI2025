package com.android.harmoniatpi.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.android.harmoniatpi.data.database.entities.ProjectEntity
import com.android.harmoniatpi.data.database.entities.UserPreferencesEntity
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.model.user.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserPreferencesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(userPreferences: UserPreferencesEntity)

    @Update
    suspend fun updateUserPreferences(userPreferences: UserPreferencesEntity)

    @Query("SELECT * FROM UserPreferencesTable")
    fun getAllUser(): Flow<List<UserPreferencesEntity>>

    @Query("SELECT * FROM UserPreferencesTable WHERE userID = :userID LIMIT 1")
    suspend fun getUserPreferences(userID: String): UserPreferencesEntity?

    @Query("""
        UPDATE UserPreferencesTable 
        SET 
            friendsList = COALESCE(:friendsList, friendsList),
            projectsList = COALESCE(:projectsList, projectsList),
            myPostsList = COALESCE(:myPostsList, myPostsList),
            friendRequestReceived = COALESCE(:friendRequestReceived, friendRequestReceived),
            friendRequestSent = COALESCE(:friendRequestSent, friendRequestSent)
        WHERE userID = :userID
    """)


    suspend fun updateSocialData(
        userID: String,
        friendsList: String? = null,
        projectsList: String? = null,
        myPostsList: String? = null,
        friendRequestReceived: String? = null,
        friendRequestSent: String? = null
    )
}