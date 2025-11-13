package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.UserPreferences
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetUserOnFirebaseByIDUseCaseTest {

    private lateinit var repository: Repository
    private lateinit var useCase: GetUserOnFirebaseByIDUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = GetUserOnFirebaseByIDUseCase(repository)
    }

    @Test
    fun `invoke calls getUserById on repository`() = runTest {
        val userId = "user123"
        val mockUser = mockk<UserPreferences>()

        coEvery { repository.getUserById(userId) } returns mockUser

        val result = useCase(userId)

        coVerify(exactly = 1) { repository.getUserById(userId) }

        assertEquals(mockUser, result)
    }

    @Test
    fun `invoke calls getUserById on repository and handles failure`() = runTest {
        val userId = "user123"

        coEvery { repository.getUserById(userId) } returns null

        val result = useCase(userId)

        coVerify(exactly = 1) { repository.getUserById(userId) }

        assertEquals(null, result)
    }
}