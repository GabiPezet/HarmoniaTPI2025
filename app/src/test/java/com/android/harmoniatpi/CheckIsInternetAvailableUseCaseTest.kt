package com.android.harmoniatpi

import com.android.harmoniatpi.di.util.NetworkUtils
import com.android.harmoniatpi.domain.usecases.CheckIsInternetAvailableUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CheckIsInternetAvailableUseCaseTest {

    private lateinit var networkUtils: NetworkUtils
    private lateinit var useCase: CheckIsInternetAvailableUseCase

    @Before
    fun setUp() {
        networkUtils = mockk()
        useCase = CheckIsInternetAvailableUseCase(networkUtils)
    }

    @Test
    fun `invoke returns true when internet is available`() = runTest {
        
        coEvery { networkUtils.isInternetAvailable() } returns true

        
        val result = useCase()

        
        assertTrue(result)
    }

    @Test
    fun `invoke returns false when internet is not available`() = runTest {
        
        coEvery { networkUtils.isInternetAvailable() } returns false

        
        val result = useCase()

        
        assertFalse(result)
    }
}