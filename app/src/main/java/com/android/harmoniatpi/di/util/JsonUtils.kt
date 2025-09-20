package com.android.harmoniatpi.di.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonUtils @Inject constructor() {
    private val gson = Gson()

    fun <T> decodeFromJson(json: String, type: Type): T {
        return gson.fromJson(json, type)
    }

    // Codificar una lista u objeto a JSON
    // Ejemplo : jsonUtils.encodeToJson(notificationList)
    fun <T> encodeToJson(data: T): String {
        return gson.toJson(data)
    }

    // Decodificar un JSON a objeto
    // Ejemplo : jsonUtils.decodeJsonToObject<LoginCredentials>(decryptedUser)
    inline fun <reified T> decodeJsonToObject(json: String): T {
        val type = object : TypeToken<T>() {}.type
        return decodeFromJson(json, type)
    }

    // Decodificar un JSON a lista de objetos
    // Ejemplo : jsonUtils.decodeJsonToListObject<NotificationOcasa>(notificationList)
    inline fun <reified T> decodeJsonToListObject(json: String): List<T> {
        val type = object : TypeToken<List<T>>() {}.type
        return decodeFromJson(json, type)
    }

}