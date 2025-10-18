package com.android.harmoniatpi.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.android.harmoniatpi.data.database.entities.MyPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MyPostDao {

    @Query("SELECT * FROM MyPostEntity")
    fun getMyPosts(): Flow<List<MyPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: MyPostEntity)

    @Update
    suspend fun updatePost(post: MyPostEntity)

    @Query("DELETE FROM MyPostEntity WHERE id = :id")
    suspend fun deletePostById(id: String)
}