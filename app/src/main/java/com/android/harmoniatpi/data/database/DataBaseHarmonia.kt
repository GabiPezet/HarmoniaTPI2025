package com.android.harmoniatpi.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.android.harmoniatpi.data.database.converters.UserPreferencesTypeConverters
import com.android.harmoniatpi.data.database.dao.ProjectDao
import com.android.harmoniatpi.data.database.dao.UserPreferencesDao
import com.android.harmoniatpi.data.database.entities.ProjectEntity
import com.android.harmoniatpi.data.database.entities.UserPreferencesEntity

@Database(
    entities = [UserPreferencesEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(UserPreferencesTypeConverters::class)
abstract class DataBaseHarmonia : RoomDatabase() {
    abstract fun userPreferencesDao(): UserPreferencesDao

}