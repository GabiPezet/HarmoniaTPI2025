package com.android.harmoniatpi.domain.usecases.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class DeleteTrackUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var deleteTrackUseCase: DeleteTrackUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        deleteTrackUseCase = DeleteTrackUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke calls deleteTrack on repository with correct id`() = runBlocking {
        val trackId = 1L
        deleteTrackUseCase(trackId)
        coVerify(exactly = 1) { audioMixerRepository.removeTrack(trackId) }
    }
}