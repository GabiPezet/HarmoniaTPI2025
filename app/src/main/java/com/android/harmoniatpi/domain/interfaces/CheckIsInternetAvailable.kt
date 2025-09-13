package com.android.harmoniatpi.domain.interfaces

interface CheckIsInternetAvailable {
    suspend operator fun invoke(): Boolean
}