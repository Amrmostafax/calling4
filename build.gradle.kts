// This is the TOP-LEVEL build file.
// It configures the build plugins for the entire project.
plugins {
    // Apply the Android Application plugin to the 'app' module
    id("com.android.application") version "8.4.0" apply false
    
    // Apply the Kotlin plugin to the 'app' module
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
}
