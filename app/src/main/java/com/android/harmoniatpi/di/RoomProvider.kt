package com.android.harmoniatpi.di

import android.content.Context
import androidx.room.Room
import com.android.harmoniatpi.data.database.DataBaseHarmonia
import com.android.harmoniatpi.data.database.dao.UserPreferencesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomProvider {

    private const val DATABASE_NAME = "harmonia_database"

    @Provides
    @Singleton
    fun provideRoomDataBase(@ApplicationContext context: Context): DataBaseHarmonia =
        Room.databaseBuilder(
            context,
            DataBaseHarmonia::class.java,
            DATABASE_NAME
        ).build()

    @Provides
    @Singleton
    fun provideUserPreferencesDao(dataBaseHarmonia: DataBaseHarmonia): UserPreferencesDao =
        dataBaseHarmonia.userPreferencesDao()
}