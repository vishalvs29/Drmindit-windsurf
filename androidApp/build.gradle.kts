plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("io.gitlab.arturbosch.detekt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    // Temporarily disabled Hilt due to Kotlin 2.0 compatibility issues
    // id("com.google.dagger.hilt.android")
    // id("kotlin-kapt")
}

android {
    namespace = "com.drmindit.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.drmindit.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }

        // Build config fields for environment - LOAD FROM ENVIRONMENT VARIABLES
        buildConfigField("String", "SUPABASE_URL", "\"${System.getenv("SUPABASE_URL") ?: "https://your-project.supabase.co"}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${System.getenv("SUPABASE_ANON_KEY") ?: "your-anon-key-here"}\"")
        
        // Manifest placeholders
        manifestPlaceholders["appAuthRedirectScheme"] = "com.drmindit.android"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            // Load signing config from local.properties or environment variables
            // DO NOT hardcode passwords here
            storeFile = file(project.findProperty("RELEASE_STORE_FILE") ?: "release.keystore")
            storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String? ?: System.getenv("RELEASE_STORE_PASSWORD")
            keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String? ?: System.getenv("RELEASE_KEY_ALIAS")
            keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String? ?: System.getenv("RELEASE_KEY_PASSWORD")
            
            // Enable V1 and V2 signing
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    // Test options
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
        unitTests.all {
            it.jvmArgs("-XX:+EnableDynamicAgentLoading")
        }
    }

    // Detekt configuration
    detekt {
        config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = true
    }
}

dependencies {
    implementation(project(":shared"))
    
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    
    // Dependency Injection - Hilt (temporarily disabled)
    // implementation("com.google.dagger:hilt-android:2.51.1")
    // implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    // kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    
    // Network
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-logging:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    
    // Media3 (ExoPlayer)
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-session:1.4.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    
    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")
    
    // Image Loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    
    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    // kapt("androidx.room:room-compiler:2.6.1")
    
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    // implementation("androidx.hilt:hilt-work:1.2.0")
    // kapt("com.google.dagger:hilt-android-compiler:2.51.1")
    
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    // Firebase (Backend)
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-config-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.24")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.24")
    testImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    // testImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    // kaptTest("com.google.dagger:hilt-android-compiler:2.51.1")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    // androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    // kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.51.1")
}
