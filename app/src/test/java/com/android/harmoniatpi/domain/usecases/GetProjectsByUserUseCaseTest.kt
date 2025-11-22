package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.domain.interfaces.Repository
import com.android.harmoniatpi.domain.helperUtil.MockHelperTestUnit
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetProjectsByUserUseCaseTest {

    private lateinit var repository: Repository
    private lateinit var useCase: GetProjectsByUserUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetProjectsByUserUseCase(repository)
    }

    @Test
    fun `invoke returns list of projects for given ownerId`() = runTest {
        val ownerId = "123"

        val expectedList = listOf(
            MockHelperTestUnit.createProject(id = "1", ownerId = ownerId),
            MockHelperTestUnit.createProject(id = "2", ownerId = ownerId)
        )

        coEvery { repository.getAllProjectsByUser(ownerId) } returns flowOf(expectedList)

        val result = useCase(ownerId)

        assertEquals(expectedList.size, result.first().size)
    }
}