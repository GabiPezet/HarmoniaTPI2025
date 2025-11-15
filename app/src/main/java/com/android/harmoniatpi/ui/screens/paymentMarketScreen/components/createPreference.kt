package com.android.harmoniatpi.ui.screens.paymentMarketScreen.components

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

suspend fun createPreference(): String? = withContext(Dispatchers.IO) {

    val url = "https://api.mercadopago.com/checkout/preferences"

    val accessToken = "TEST-5848709767478891-111416-ba9a312e49225b9c6caacf6f4be1d9dc-1058147682"

    val client = OkHttpClient()

    val itemsArray = JSONArray().apply {
        put(
            JSONObject().apply {
                put("title", "Membresía Premium")
                put("quantity", 1)
                put("currency_id", "ARS")
                put("unit_price", 100)
            }
        )
    }

    val bodyJson = JSONObject().apply {
        put("items", itemsArray)
        put("auto_return", "approved")
        put("back_urls", JSONObject().apply {
            put("success", "HarmoniaTPI://payment/approved")
        })
    }

    val body = bodyJson
        .toString()
        .toRequestBody("application/json".toMediaType())

    val request = Request.Builder()
        .url(url)
        .post(body)
        .addHeader("Authorization", "Bearer $accessToken")
        .build()

    val response = client.newCall(request).execute()
    val responseBody = response.body?.string()

    if (response.isSuccessful && responseBody != null) {
        val json = JSONObject(responseBody)
        return@withContext json.getString("init_point")
    }

    return@withContext null
}