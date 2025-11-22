package com.android.harmoniatpi.domain

import com.android.harmoniatpi.domain.interfaces.CheckIsInternetAvailable
import javax.inject.Inject

class FakeCheckIsInternetAvailable @Inject constructor() : CheckIsInternetAvailable {
    override suspend fun invoke(): Boolean = false
}