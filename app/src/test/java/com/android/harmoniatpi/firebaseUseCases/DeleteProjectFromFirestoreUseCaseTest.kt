package com.android.harmoniatpi.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.DeleteProjectFromFirestoreUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeleteProjectFromFirestoreUseCaseTest {

    private lateinit var repository: Repository
    private lateinit var useCase: DeleteProjectFromFirestoreUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = DeleteProjectFromFirestoreUseCase(repository)
    }

    @Test
    fun `invoke calls deleteProjectFromFirestore on repository`() = runTest {
        val projectId = "project123"
        coEvery { repository.deleteProjectFromFirestore(projectId) } returns Result.success(Unit)

        val result = useCase(projectId)

        coVerify(exactly = 1) { repository.deleteProjectFromFirestore(projectId) }
        assertTrue(result.isSuccess)
    }
}