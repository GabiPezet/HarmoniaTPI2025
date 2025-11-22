package com.android.harmoniatpi

import com.android.harmoniatpi.domain.interfaces.SongRepository
import com.android.harmoniatpi.domain.usecases.GetSongDetailsUseCase
import com.android.harmoniatpi.helperUtil.MockHelperTestUnit
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetSongDetailsUseCaseTest {

    private lateinit var repository: SongRepository
    private lateinit var useCase: GetSongDetailsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetSongDetailsUseCase(repository)
    }

    @Test
    fun `invoke returns success with SongDetails`() = runTest {
        // Arrange
        val songId = "song123"
        val expected = MockHelperTestUnit.mockSongDetails()

        coEvery { repository.getSongDetails(songId) } returns Result.success(expected)

        // Act
        val result = useCase(songId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `invoke returns failure when repository throws`() = runTest {
        // Arrange
        val songId = "song123"
        val exception = RuntimeException("Song not found")

        coEvery { repository.getSongDetails(songId) } returns Result.failure(exception)

        // Act
        val result = useCase(songId)

        // Assert
        assertTrue(result.isFailure)
        assertEquals("Song not found", result.exceptionOrNull()?.message)
    }
}
