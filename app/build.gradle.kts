plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.3.6"
    id("com.google.dagger.hilt.android") version "2.59.2"
}

android {
    namespace = "com.example.safedriveai"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.safedriveai"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.2.alpha"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.remote.creation.core)
    implementation(libs.androidx.material3)
    implementation(libs.osmdroid.android)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.gson)
    implementation(libs.listenablefuture)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compiler)
    implementation(libs.androidx.compose.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ── Google Location Services API ─────────────────────────────────────────
    implementation(libs.play.services.location.v2120)

    // ── DATA PERSISTENCE (Room) ───────────────────────────
    val roomVersion = "2.6.1"
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // Usamos KSP, no kapt

    // ── PUENTE ENTRE HILT Y JETPACK COMPOSE ───────────────────────
    implementation(libs.androidx.hilt.navigation.compose)

    // ── DEPENDENCY INJECTION (Hilt) ───────────────────────
    val hiltVersion = "2.59.2"
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // ── COROUTINES (Necesarias para Room y Backend) ───────
    implementation(libs.kotlinx.coroutines.android)

    // ── FIREBASE / BACKEND ────────────────────────────────
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
}