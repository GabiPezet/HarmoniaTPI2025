package com.android.harmoniatpi.di

import com.android.harmoniatpi.data.ExoAudioPlayerRepositoryImpl
import com.android.harmoniatpi.domain.interfaces.ExoAudioPlayerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExoPlayerModule {

    @Binds
    @Singleton
    abstract fun bindExoAudioPlayerRepository(
        exoAudioPlayerRepositoryImpl: ExoAudioPlayerRepositoryImpl
    ): ExoAudioPlayerRepository
}