package com.android.harmoniatpi.domain.usecases.firebaseUseCases

import com.android.harmoniatpi.data.local.model.ProjectFirebaseModel
import com.android.harmoniatpi.domain.interfaces.Repository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpsertProjectInFirestoreUseCaseTest {

    private lateinit var repository: Repository
    private lateinit var upsertProjectInFirestoreUseCase: UpsertProjectInFirestoreUseCase

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        upsertProjectInFirestoreUseCase = UpsertProjectInFirestoreUseCase(repository)
    }

    @Test
    fun `invoke calls upsertProjectInFirestore on repository`() = runTest {
        
        val projectModel = mockk<ProjectFirebaseModel>()
        coEvery { repository.upsertProjectInFirestore(projectModel) } returns Result.success(Unit)

        
        val result = upsertProjectInFirestoreUseCase(projectModel)

        
        coVerify(exactly = 1) { repository.upsertProjectInFirestore(projectModel) }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        
        val projectModel = mockk<ProjectFirebaseModel>()
        val exception = Exception("Firestore error")
        coEvery { repository.upsertProjectInFirestore(projectModel) } returns Result.failure(exception)

        
        val result = upsertProjectInFirestoreUseCase(projectModel)

        
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}