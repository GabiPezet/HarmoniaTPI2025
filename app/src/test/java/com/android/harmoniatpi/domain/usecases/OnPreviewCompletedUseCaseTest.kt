package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.AudioMixerRepository
import com.android.harmoniatpi.domain.usecases.audioUseCases.OnPreviewCompletedUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
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
        val completionFlow = MutableSharedFlow<Unit>()
        coEvery { audioMixerRepository.onPreviewCompleted() } returns completionFlow

        val resultFlow = useCase()

        var completed = false
        val job = launch {
            resultFlow.first()
            completed = true
        }

        completionFlow.emit(Unit)
        job.join()

        assert(completed)
    }
}