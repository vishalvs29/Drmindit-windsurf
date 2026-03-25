package com.drmindit.shared.data.config

import kotlin.properties.Delegates

object AppConfig {
    // Environment detection
    private val environment: Environment by lazy {
        when (System.getProperty("app.env") ?: "development") {
            "production" -> Environment.PRODUCTION
            "staging" -> Environment.STAGING
            "development" -> Environment.DEVELOPMENT
            else -> Environment.DEVELOPMENT
        }
    }
    
    // Supabase Configuration - LOAD FROM ENVIRONMENT VARIABLES
    val supabaseUrl: String by lazy {
        when (environment) {
            Environment.PRODUCTION -> System.getenv("SUPABASE_PROD_URL") ?: System.getProperty("supabase.prod.url") ?: "https://your-prod-project.supabase.co"
            Environment.STAGING -> System.getenv("SUPABASE_STAGING_URL") ?: System.getProperty("supabase.staging.url") ?: "https://your-staging-project.supabase.co"
            Environment.DEVELOPMENT -> System.getenv("SUPABASE_DEV_URL") ?: System.getProperty("supabase.dev.url") ?: "https://your-dev-project.supabase.co"
        }
    }
    
    val supabaseAnonKey: String by lazy {
        when (environment) {
            Environment.PRODUCTION -> System.getenv("SUPABASE_PROD_ANON_KEY") ?: System.getProperty("supabase.prod.anon_key") ?: ""
            Environment.STAGING -> System.getenv("SUPABASE_STAGING_ANON_KEY") ?: System.getProperty("supabase.staging.anon_key") ?: ""
            Environment.DEVELOPMENT -> System.getenv("SUPABASE_DEV_ANON_KEY") ?: System.getProperty("supabase.dev.anon_key") ?: ""
        }
    }
    
    val supabaseServiceKey: String by lazy {
        when (environment) {
            Environment.PRODUCTION -> System.getenv("SUPABASE_PROD_SERVICE_KEY") ?: System.getProperty("supabase.prod.service_key") ?: ""
            Environment.STAGING -> System.getenv("SUPABASE_STAGING_SERVICE_KEY") ?: System.getProperty("supabase.staging.service_key") ?: ""
            Environment.DEVELOPMENT -> System.getenv("SUPABASE_DEV_SERVICE_KEY") ?: System.getProperty("supabase.dev.service_key") ?: ""
        }
    }
    
    // API Configuration
    val apiBaseUrl: String get() = supabaseUrl
    val apiTimeout: Long by lazy {
        when (environment) {
            Environment.PRODUCTION -> 30000L
            Environment.STAGING -> 25000L
            Environment.DEVELOPMENT -> 15000L
        }
    }
    
    val retryAttempts: Int by lazy {
        when (environment) {
            Environment.PRODUCTION -> 3
            Environment.STAGING -> 2
            Environment.DEVELOPMENT -> 1
        }
    }
    
    // Logging Configuration
    val isDebugMode: Boolean get() = environment == Environment.DEVELOPMENT
    val logLevel: LogLevel by lazy {
        when (environment) {
            Environment.PRODUCTION -> LogLevel.ERROR
            Environment.STAGING -> LogLevel.INFO
            Environment.DEVELOPMENT -> LogLevel.DEBUG
        }
    }
    
    // Feature Flags
    val isCrisisDetectionEnabled: Boolean by lazy {
        when (environment) {
            Environment.PRODUCTION -> System.getProperty("feature.crisis_detection")?.toBoolean() ?: true
            Environment.STAGING -> System.getProperty("feature.crisis_detection")?.toBoolean() ?: true
            Environment.DEVELOPMENT -> System.getProperty("feature.crisis_detection")?.toBoolean() ?: true
        }
    }
    
    val isAnalyticsEnabled: Boolean by lazy {
        when (environment) {
            Environment.PRODUCTION -> System.getProperty("feature.analytics")?.toBoolean() ?: true
            Environment.STAGING -> System.getProperty("feature.analytics")?.toBoolean() ?: true
            Environment.DEVELOPMENT -> System.getProperty("feature.analytics")?.toBoolean() ?: false
        }
    }
    
    val isPushNotificationsEnabled: Boolean by lazy {
        when (environment) {
            Environment.PRODUCTION -> System.getProperty("feature.push_notifications")?.toBoolean() ?: true
            Environment.STAGING -> System.getProperty("feature.push_notifications")?.toBoolean() ?: false
            Environment.DEVELOPMENT -> System.getProperty("feature.push_notifications")?.toBoolean() ?: false
        }
    }
    
    // Audio Configuration
    val audioBufferSize: Int by lazy {
        when (environment) {
            Environment.PRODUCTION -> 8192
            Environment.STAGING -> 4096
            Environment.DEVELOPMENT -> 2048
        }
    }
    
    val maxAudioCacheSize: Long by lazy {
        when (environment) {
            Environment.PRODUCTION -> 100 * 1024 * 1024L // 100MB
            Environment.STAGING -> 50 * 1024 * 1024L   // 50MB
            Environment.DEVELOPMENT -> 10 * 1024 * 1024L // 10MB
        }
    }
    
    // Cache Configuration
    val maxMemoryCacheSize: Long by lazy {
        when (environment) {
            Environment.PRODUCTION -> 32 * 1024 * 1024L // 32MB
            Environment.STAGING -> 16 * 1024 * 1024L   // 16MB
            Environment.DEVELOPMENT -> 8 * 1024 * 1024L  // 8MB
        }
    }
    
    val diskCacheSize: Long by lazy {
        when (environment) {
            Environment.PRODUCTION -> 200 * 1024 * 1024L // 200MB
            Environment.STAGING -> 100 * 1024 * 1024L   // 100MB
            Environment.DEVELOPMENT -> 50 * 1024 * 1024L  // 50MB
        }
    }
    
    // Network Configuration
    val connectTimeout: Long by lazy {
        when (environment) {
            Environment.PRODUCTION -> 15000L
            Environment.STAGING -> 10000L
            Environment.DEVELOPMENT -> 5000L
        }
    }
    
    val readTimeout: Long by lazy {
        when (environment) {
            Environment.PRODUCTION -> 30000L
            Environment.STAGING -> 20000L
            Environment.DEVELOPMENT -> 10000L
        }
    }
    
    val writeTimeout: Long by lazy {
        when (environment) {
            Environment.PRODUCTION -> 30000L
            Environment.STAGING -> 20000L
            Environment.DEVELOPMENT -> 10000L
        }
    }
    
    // App Configuration
    val appVersion: String by lazy {
        System.getProperty("app.version") ?: "1.0.0"
    }
    
    val buildNumber: Int by lazy {
        System.getProperty("app.build_number")?.toIntOrNull() ?: 1
    }
    
    val minSdkVersion: Int by lazy {
        System.getProperty("app.min_sdk")?.toIntOrNull() ?: 24
    }
    
    val targetSdkVersion: Int by lazy {
        System.getProperty("app.target_sdk")?.toIntOrNull() ?: 34
    }
    
    // Security Configuration
    val isCertificatePinningEnabled: Boolean by lazy {
        environment == Environment.PRODUCTION
    }
    
    val isNetworkLoggingEnabled: Boolean get() = isDebugMode
    
    // Monitoring Configuration
    val sentryDsn: String? by lazy {
        when (environment) {
            Environment.PRODUCTION -> System.getProperty("sentry.dsn")
            Environment.STAGING -> System.getProperty("sentry.dsn")
            Environment.DEVELOPMENT -> null
        }
    }
    
    val firebaseProjectId: String? by lazy {
        when (environment) {
            Environment.PRODUCTION -> System.getProperty("firebase.project_id")
            Environment.STAGING -> System.getProperty("firebase.project_id")
            Environment.DEVELOPMENT -> null
        }
    }
    
    // Helper methods
    fun isProduction(): Boolean = environment == Environment.PRODUCTION
    fun isStaging(): Boolean = environment == Environment.STAGING
    fun isDevelopment(): Boolean = environment == Environment.DEVELOPMENT
    
    fun getEnvironmentName(): String = environment.name.lowercase()
    
    fun getBuildFlavor(): String = when (environment) {
        Environment.PRODUCTION -> "release"
        Environment.STAGING -> "staging"
        Environment.DEVELOPMENT -> "debug"
    }
    
    // Validation
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (supabaseUrl.isBlank()) {
            errors.add("Supabase URL is not configured")
        }
        
        if (supabaseAnonKey.isBlank()) {
            errors.add("Supabase Anon Key is not configured")
        }
        
        if (environment == Environment.PRODUCTION) {
            if (supabaseServiceKey.isBlank()) {
                errors.add("Supabase Service Key is required for production")
            }
            
            if (!isCertificatePinningEnabled) {
                errors.add("Certificate pinning should be enabled in production")
            }
        }
        
        return errors
    }
}

enum class Environment {
    DEVELOPMENT,
    STAGING,
    PRODUCTION
}

enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}
