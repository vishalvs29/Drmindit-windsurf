plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization") version "1.9.20"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    // Temporarily disable iOS targets to focus on Android
    // listOf(
    //     iosX64(),
    //     iosArm64(),
    //     iosSimulatorArm64()
    // ).forEach { iosTarget ->
    //     iosTarget.binaries.framework {
    //         baseName = "Shared"
    //         isStatic = true
    //     }
    // }
    
    sourceSets {
        val supabaseVersion = "2.5.0"
        
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            implementation("io.ktor:ktor-client-core:2.3.5")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.5")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")
            implementation("io.ktor:ktor-client-logging:2.3.5")
            implementation("io.ktor:ktor-client-auth:2.3.5")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
            
            // Supabase dependencies
            implementation("io.github.jan-tennert.supabase:postgrest-kt:$supabaseVersion")
            implementation("io.github.jan-tennert.supabase:gotrue-kt:$supabaseVersion")
            implementation("io.github.jan-tennert.supabase:storage-kt:$supabaseVersion")
            implementation("io.github.jan-tennert.supabase:realtime-kt:$supabaseVersion")
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        
        androidMain.dependencies {
            implementation("io.ktor:ktor-client-android:2.3.5")
            implementation("androidx.security:security-crypto:1.1.0-alpha06")
        }
        
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:2.3.5")
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
