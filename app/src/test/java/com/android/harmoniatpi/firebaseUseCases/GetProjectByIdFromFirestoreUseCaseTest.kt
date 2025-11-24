package com.android.harmoniatpi.firebaseUseCases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.model.project.Project
import com.android.harmoniatpi.domain.usecases.firebaseUseCases.GetProjectByIdFromFirestoreUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetProjectByIdFromFirestoreUseCaseTest {

    private lateinit var repository: Repository
    private lateinit var useCase: GetProjectByIdFromFirestoreUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        useCase = GetProjectByIdFromFirestoreUseCase(repository)
    }

    @Test
    fun `invoke calls getProjectByIdFromFirestore on repository`() = runTest {
        val projectId = "project123"
        val mockProject = mockk<Project>()
        coEvery { repository.getProjectByIdFromFirestore(projectId) } returns mockProject

        val result = useCase(projectId)

        coVerify(exactly = 1) { repository.getProjectByIdFromFirestore(projectId) }
        assertEquals(mockProject, result)
    }
}