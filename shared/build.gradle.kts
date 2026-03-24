plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("io.insert-koin")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            
            // Koin for KMP
            implementation("io.insert-koin:koin-core:3.5.3")
            implementation("io.insert-koin:koin-compose:3.5.3")
            
            // Kotlin Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            
            // Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            
            // Ktor for networking
            implementation("io.ktor:ktor-client-core:2.3.6")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.6")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.6")
            
            // Date/Time
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
        }
        
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-android:2.3.6")
        }
        
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:2.3.6")
        }
    }
}

android {
    namespace = "com.drmindit.shared"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
