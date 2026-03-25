plugins {
    // Kotlin Multiplatform plugin
    kotlin("jvm") version "2.0.0" apply false
    kotlin("android") version "2.0.0" apply false
    kotlin("multiplatform") version "2.0.0" apply false
    
    // Android plugins
    id("com.android.application") version "8.5.0" apply false
    id("com.android.library") version "8.5.0" apply false
    
    // Compose plugins
    id("org.jetbrains.compose") version "1.6.10" apply false
    
    // Koin
    id("io.insert-koin") version "3.5.6" apply false
    
    // Static Analysis
    id("io.gitlab.arturbosch.detekt") version "1.23.5" apply false
    
    // Security
    id("org.owasp.dependencycheck") version "9.2.0" apply false
    
    // Firebase
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
}
