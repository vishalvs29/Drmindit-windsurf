plugins {
    // Kotlin Multiplatform plugin
    kotlin("jvm") version "1.9.20" apply false
    kotlin("android") version "1.9.20" apply false
    kotlin("multiplatform") version "1.9.20" apply false
    
    // Android plugins
    id("com.android.application") version "8.1.2" apply false
    id("com.android.library") version "8.1.2" apply false
    
    // Compose plugins
    id("org.jetbrains.compose") version "1.5.10" apply false
    
    // Dependency injection
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    
    // Koin
    id("io.insert-koin") version "3.5.3" apply false
}
