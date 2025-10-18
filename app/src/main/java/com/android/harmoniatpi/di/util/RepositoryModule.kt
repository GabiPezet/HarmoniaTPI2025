package com.android.harmoniatpi.di.util

import com.android.harmoniatpi.data.RepositoryImpl
import com.android.harmoniatpi.data.song.SongRepositoryImpl
import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.interfaces.SongRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun provideRepository(impl: RepositoryImpl): Repository = impl

    @Provides
    fun provideSongRepository (impl: SongRepositoryImpl): SongRepository = impl
}