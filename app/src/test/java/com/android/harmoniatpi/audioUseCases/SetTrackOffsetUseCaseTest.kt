package com.android.harmoniatpi.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.SetTrackOffsetUseCase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SetTrackOffsetUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var setTrackOffsetUseCase: SetTrackOffsetUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        setTrackOffsetUseCase = SetTrackOffsetUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls setTrackOffset on repository`() {
        val trackId = 1L
        val offsetMs = 500L
        setTrackOffsetUseCase(trackId, offsetMs)
        verify(exactly = 1) { audioMixerRepository.setTrackOffset(trackId, offsetMs) }
    }
}