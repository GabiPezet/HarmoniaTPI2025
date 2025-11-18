package com.android.harmoniatpi.data.remote.model

import com.google.gson.annotations.SerializedName

data class SubscriptionRequest(
    @SerializedName("reason") val reason: String,
    @SerializedName("payer_email") val payerEmail: String,
    @SerializedName("auto_recurring") val autoRecurring: AutoRecurring,
    @SerializedName("back_url") val backUrl: String,
)

data class AutoRecurring(
    @SerializedName("frequency") val frequency: Int = 1,
    @SerializedName("frequency_type") val frequencyType: String = "months",
    @SerializedName("transaction_amount") val transactionAmount: Double,
    @SerializedName("currency_id") val currencyId: String = "ARS"
)

data class SubscriptionResponse(
    @SerializedName("id") val id: String,
    @SerializedName("init_point") val initPoint: String,
    @SerializedName("payer_id") val payerId: Long?
)