package com.android.harmoniatpi.roomUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import com.android.harmoniatpi.domain.usecases.roomUseCases.GetUserPreferencesUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetMyPostFromDataBaseUseCaseTest {

    private lateinit var repository: Repository
    private lateinit var getUserPreferencesUseCase: GetUserPreferencesUseCase

    @Before
    fun setUp() {
        repository = mockk()
        getUserPreferencesUseCase = GetUserPreferencesUseCase(repository)
    }

    @Test
    fun `invoke returns user preferences from repository`() = runTest {

        val mockPreferences = mockk<UserPreferences>()
        coEvery { repository.getUserPreferences() } returns mockPreferences

        val result = getUserPreferencesUseCase()

        assertEquals(mockPreferences, result)
    }

    @Test
    fun `invoke returns null when repository returns null`() = runTest {
        coEvery { repository.getUserPreferences() } returns null

        val result = getUserPreferencesUseCase()

        assertEquals(null, result)
    }
}