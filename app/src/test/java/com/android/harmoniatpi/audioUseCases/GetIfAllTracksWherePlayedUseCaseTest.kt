package com.android.harmoniatpi.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.GetIfAllTracksWherePlayedUseCase
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetIfAllTracksWherePlayedUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var audioMixerRepository: AudioMixerRepository

    private lateinit var getIfAllTracksWherePlayedUseCase: GetIfAllTracksWherePlayedUseCase

    @Before
    fun setUp() {
        getIfAllTracksWherePlayedUseCase = GetIfAllTracksWherePlayedUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke returns true when repository returns true`() = runBlocking {
        val value = MutableStateFlow(true)
        coEvery { audioMixerRepository.allTracksWerePlayed() } returns value

        val result = getIfAllTracksWherePlayedUseCase()

        assertTrue(result.first())
    }

    @Test
    fun `invoke returns false when repository returns false`() = runBlocking {
        val value = MutableStateFlow(false)
        coEvery { audioMixerRepository.allTracksWerePlayed() } returns value

        val result = getIfAllTracksWherePlayedUseCase()

        assertFalse(result.first())
    }
}
