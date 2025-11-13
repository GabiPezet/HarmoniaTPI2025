package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LogOutFirebaseUseCaseTest {

    private lateinit var repository: Repository
    private lateinit var useCase: LogOutFirebaseUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = LogOutFirebaseUseCase(repository)
    }

    @Test
    fun `invoke calls logOutUser on repository`() = runTest {
        useCase()
        coVerify(exactly = 1) { repository.logOutUser() }
    }
}