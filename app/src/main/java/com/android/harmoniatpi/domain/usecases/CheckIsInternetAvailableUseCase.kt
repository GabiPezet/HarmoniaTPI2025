package com.android.harmoniatpi.domain.usecases

import com.android.harmoniatpi.di.util.NetworkUtils
import com.android.harmoniatpi.domain.interfaces.CheckIsInternetAvailable
import javax.inject.Inject

class CheckIsInternetAvailableUseCase @Inject constructor(
    private val networkUtils: NetworkUtils
) : CheckIsInternetAvailable {
    override suspend operator fun invoke(): Boolean {
        return networkUtils.isInternetAvailable()
    }
}
