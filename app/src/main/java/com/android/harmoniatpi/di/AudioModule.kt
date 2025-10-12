package com.android.harmoniatpi.di

import com.android.harmoniatpi.data.audio.mixer.AudioMixerRepositoryImpl
import com.android.harmoniatpi.domain.interfaces.AudioPlayer
import com.android.harmoniatpi.data.audio.player.PcmAudioPlayer
import com.android.harmoniatpi.domain.interfaces.AudioRecorder
import com.android.harmoniatpi.data.audio.record.AudioRecorderRepositoryImpl
import com.android.harmoniatpi.data.audio.record.PcmAudioRecorder
import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
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

    @Binds
    @Singleton
    abstract fun bindAudioMixerRepository(impl: AudioMixerRepositoryImpl): AudioMixerRepository

    @Binds
    abstract fun bindAudioPlayer(impl: PcmAudioPlayer): AudioPlayer
}
