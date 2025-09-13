package com.android.harmoniatpi.di

import com.android.harmoniatpi.di.util.AnalyticsManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseAnalytics(): FirebaseAnalytics = Firebase.analytics

    @Provides
    fun provideAnalyticsManager(analytics: FirebaseAnalytics): AnalyticsManager =
        AnalyticsManager(analytics)

    @Provides
    @Singleton
    fun provideFirebaseRealTimeDataBase(): FirebaseDatabase = Firebase.database
}