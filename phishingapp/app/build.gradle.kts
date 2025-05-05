plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

android {
    namespace = "com.example.phishingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.phishingapp"
        minSdk = 29
        targetSdk = 34 // Updated to match compileSdk
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += setOf("armeabi-v7a", "arm64-v8a") // Include only these ABIs
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true // Enable code shrinking
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    packagingOptions {
        resources {
            excludes += setOf(
                "lib/x86_64/libtensorflowlite_jni.so",
                "lib/x86/libtensorflowlite_jni.so",
                "lib/armeabi-v7a/libtensorflowlite_jni.so",
                "lib/arm64-v8a/libtensorflowlite_jni.so"
            )
        }
    }
}

dependencies {
    implementation(libs.mpandroidchart) // Latest version: 3.1.0
    implementation(libs.retrofit) // Latest version: 2.9.0
    implementation(libs.gson) // Latest version: 2.10.1
    implementation(libs.androidx.core.ktx) // Latest version: 1.12.0
    implementation(libs.androidx.appcompat) // Latest version: 1.6.1
    implementation(libs.material) // Latest version: 1.9.0
    implementation(libs.androidx.activity) // Latest version: 1.8.0
    implementation(libs.androidx.constraintlayout) // Latest version: 2.1.4
    implementation(libs.litert) // Ensure this is the latest version
    implementation(libs.vision.common) // Ensure this is the latest version
    implementation(libs.play.services.mlkit.text.recognition.common) // Latest version: 20.5.0
    implementation(libs.play.services.mlkit.text.recognition) // Latest version: 20.5.0
    implementation(libs.core) // Ensure this is the latest version
    implementation(libs.androidx.foundation.layout.android) // Latest version: 1.4.0
    implementation(libs.androidx.material3.android)
    implementation(libs.volley) // Latest version: 1.1.0
    testImplementation(libs.junit) // Latest version: 4.13.2
    androidTestImplementation(libs.androidx.junit) // Latest version: 1.1.5
    androidTestImplementation(libs.androidx.espresso.core) // Latest version: 3.5.1
    implementation("androidx.compose.runtime:runtime:1.6.0")
    implementation("androidx.compose.compiler:compiler:1.5.10")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.2.1")
    implementation("io.ktor:ktor-client-okhttp:2.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    implementation("androidx.multidex:multidex:2.0.1")
}