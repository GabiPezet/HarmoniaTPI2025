package com.android.harmoniatpi.di

import com.android.harmoniatpi.data.song.SongRepositoryImpl
import com.android.harmoniatpi.domain.interfaces.SongRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SongVersionModule {

    @Binds
    @Singleton
    abstract fun bindSongVersionRepository(
        songRepositoryImpl: SongRepositoryImpl
    ): SongRepository
}