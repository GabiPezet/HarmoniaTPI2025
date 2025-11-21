package com.android.harmoniatpi.data.remote

import com.android.harmoniatpi.data.remote.model.SubscriptionRequest
import com.android.harmoniatpi.data.remote.model.SubscriptionResponse
import com.android.harmoniatpi.data.remote.model.SubscriptionStatusUpdateRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MercadoPagoApi {
    @POST("preapproval")
    suspend fun createSubscription(
        @Header("Authorization") token: String,
        @Body request: SubscriptionRequest
    ): SubscriptionResponse

    @PUT("preapproval/{id}")
    suspend fun cancelSubscription(
        @Header("Authorization") token: String,
        @Path("id") preapprovalId: String,
        @Body status: SubscriptionStatusUpdateRequest
    ): SubscriptionResponse
}