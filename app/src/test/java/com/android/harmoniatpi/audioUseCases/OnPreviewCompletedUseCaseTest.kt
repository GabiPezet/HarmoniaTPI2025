package com.android.harmoniatpi.audioUseCases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.OnPreviewCompletedUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OnPreviewCompletedUseCaseTest {

    private lateinit var audioMixerRepository: AudioMixerRepository
    private lateinit var useCase: OnPreviewCompletedUseCase

    @Before
    fun setUp() {
        audioMixerRepository = mockk(relaxed = true)
        useCase = OnPreviewCompletedUseCase(audioMixerRepository)
    }

    @Test
    fun `invoke returns completion flow from repository`() = runTest {
        val completionFlow = MutableSharedFlow<Unit>(replay = 1)

        every { audioMixerRepository.onPreviewCompleted() } returns completionFlow

        val resultFlow = useCase()

        completionFlow.emit(Unit)

        val result = resultFlow.first()

        assertEquals(Unit, result)
    }
}