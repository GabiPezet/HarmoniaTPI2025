package com.android.harmoniatpi.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.LoginWithGoogleUseCase
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginWithGoogleUseCaseTest {

    private lateinit var repository: Repository
    private lateinit var useCase: LoginWithGoogleUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = LoginWithGoogleUseCase(repository)
    }

    @Test
    fun `invoke calls signInWithGoogle on repository`() = runTest {
        val idToken = "googleToken123"
        val mockUser = mockk<FirebaseUser>()
        coEvery { repository.signInWithGoogle(idToken) } returns Result.success(mockUser)

        val result = useCase(idToken)

        coVerify(exactly = 1) { repository.signInWithGoogle(idToken) }
        assertTrue(result.isSuccess)
    }
}