plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.daggerHiltAndroid)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.performance)
}

android {
    namespace = "com.android.harmoniatpi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.android.harmoniatpi"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Básicos
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Hilt testing
    androidTestImplementation(libs.google.dagger.hilt.android.testing)
    kspAndroidTest(libs.google.dagger.hilt.android.compiler)
    // Coroutines y Mockk UnitTest
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    // Dagger + Hilt
    implementation(libs.google.dagger.hilt.android)
    ksp(libs.google.dagger.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    // Retrofit
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    // Coil
    implementation (libs.coil.compose)
    implementation (libs.androidx.runtime)
    // Dagger testing
    androidTestImplementation(libs.google.dagger.hilt.android.testing)
    testImplementation(libs.google.dagger.hilt.android.testing)
    //Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.common)
    //Serialization and Navigation 2.8.0
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation.compose)
    //Material
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.extended)
    implementation(libs.androidx.material3.window.size.class1)
    //Splash
    implementation(libs.core.splashscreen)
    //DataStore
    implementation(libs.androidx.datastore.preferences)
    // map
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)
    // ML Kit Barcode Scanning
    implementation(libs.barcode.scanning)
    // ZXing
    implementation(libs.zxing.android.embedded)
    // Accompanist System Color
    implementation (libs.accompanist.systemuicontroller)
    // Firebase Implementation
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.perf)
    implementation(libs.firebase.config)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.realtime.database)
    // Lottie
    implementation(libs.lottie.compose)
}