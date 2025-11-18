package com.android.harmoniatpi.data.remote

import com.android.harmoniatpi.data.remote.model.SubscriptionRequest
import com.android.harmoniatpi.data.remote.model.SubscriptionResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface MercadoPagoApi {
    @POST("preapproval")
    suspend fun createSubscription(
        @Header("Authorization") token: String,
        @Body request: SubscriptionRequest
    ): SubscriptionResponse
}