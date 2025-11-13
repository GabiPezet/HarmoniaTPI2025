package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RegisterUserFirebaseUseCaseTest {

    private lateinit var repository: Repository
    private lateinit var useCase: RegisterUserFirebaseUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = RegisterUserFirebaseUseCase(repository)
    }

    @Test
    fun `invoke calls registerUserFirebase on repository`() = runTest {
        val email = "test@example.com"
        val pass = "123456"
        val name = "Test"
        val lastName = "User"
        coEvery { repository.registerUserFirebase(email, pass, name, lastName) } returns Result.success(mockk())

        val result = useCase(email, pass, name, lastName)

        coVerify(exactly = 1) { repository.registerUserFirebase(email, pass, name, lastName) }
        assertTrue(result.isSuccess)
    }
}