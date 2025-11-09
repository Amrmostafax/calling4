/*
 * This is the full build file for your Android App module.
 * It tells Android how to build your app and what libraries it needs.
 */

// These are the plugins that build your app
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    // IMPORTANT: Make sure this namespace matches your package name
    // (the one in your MainActivity.kt and AndroidManifest.xml)
    namespace = "com.example.myvoicebackend"
    compileSdk = 34 // This is the standard modern Android version

    defaultConfig {
        // This should also match your namespace
        applicationId = "com.example.myvoicebackend" 
        
        minSdk = 24 // Supports Android 7.0 and newer
        targetSdk = 34
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
    
    // Configures the Java version for compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

// This is the libraries section
dependencies {

    // Standard Android libraries
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // --- NETWORKING LIBRARY ---
    // Library for making network calls to Vercel
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // --- KOTLIN COROUTINES (THESE WERE MISSING!) ---
    // These are required for async operations in MainActivity
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Standard testing libraries (you can leave these)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
