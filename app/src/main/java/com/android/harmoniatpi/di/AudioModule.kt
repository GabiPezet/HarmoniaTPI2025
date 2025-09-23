package com.android.harmoniatpi.di

import com.android.harmoniatpi.data.audio.record.AudioRecorder
import com.android.harmoniatpi.data.audio.record.AudioRecorderRepositoryImpl
import com.android.harmoniatpi.data.audio.record.PcmAudioRecorder
import com.android.harmoniatpi.domain.interfaces.AudioRecorderRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {
    @Binds
    @Singleton
    abstract fun bindAudioRecorderRepository(impl: AudioRecorderRepositoryImpl): AudioRecorderRepository

    @Binds
    abstract fun bindAudioRecorder(impl: PcmAudioRecorder): AudioRecorder
}
