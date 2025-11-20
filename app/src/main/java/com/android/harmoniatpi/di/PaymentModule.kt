package com.android.harmoniatpi.di

import com.android.harmoniatpi.data.remote.MercadoPagoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PaymentModule {

    @Provides
    @Singleton
    fun provideMercadoPagoApi(): MercadoPagoApi {
        return Retrofit.Builder()
            .baseUrl("https://api.mercadopago.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MercadoPagoApi::class.java)
    }
}