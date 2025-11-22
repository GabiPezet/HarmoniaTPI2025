package com.android.harmoniatpi

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.usecases.GetProjectByIdUseCase
import com.android.harmoniatpi.helperUtil.MockHelperTestUnit
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetProjectByIdUseCaseTest {

    private lateinit var repository: Repository
    private lateinit var useCase: GetProjectByIdUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetProjectByIdUseCase(repository)
    }

    @Test
    fun `invoke returns project when repository returns project`() = runTest {
        val projectId = "123"
        val expectedProject = MockHelperTestUnit.createProject(id = "abc123")

        coEvery { repository.getProjectById(projectId) } returns expectedProject

        val result = useCase(projectId)

        assertEquals(expectedProject, result)
    }

    @Test
    fun `invoke throws exception when repository throws exception`() = runTest {
        // Arrange
        val projectId = "not-found"
        val expectedException = RuntimeException("Project not found")

        coEvery { repository.getProjectById(projectId) } throws expectedException

        try {
            // Act
            useCase(projectId)
            assert(false) { "Expected exception was not thrown" }
        } catch (e: Exception) {
            // Assert
            assertEquals(expectedException.message, e.message)
        }
    }
}
