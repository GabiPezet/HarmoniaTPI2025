package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.SetTrackPlaybackRangeUseCase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SetTrackPlaybackRangeUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var setTrackPlaybackRangeUseCase: SetTrackPlaybackRangeUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        setTrackPlaybackRangeUseCase = SetTrackPlaybackRangeUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls setPlaybackRange on repository`() {
        val trackId = 1L
        val startMs = 1000L
        val endMs = 5000L
        val totalMs = 10000L
        setTrackPlaybackRangeUseCase(trackId, startMs, endMs, totalMs)
        verify(exactly = 1) { audioMixerRepository.setPlaybackRange(trackId, startMs, endMs, totalMs) }
    }
}