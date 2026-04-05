package com.drmindit.shared.data.config

object BuildConfig {
    // These values are set at build time from build.gradle
    const val DEBUG = true 
    const val APPLICATION_ID = "com.drmindit.android"
    const val VERSION_CODE = 1
    const val VERSION_NAME = "1.0.0"
    const val API_VERSION = "v1"
    
    // Build type specific values
    const val BUILD_TYPE = "debug"
    const val FLAVOR = ""
    
    // API endpoints
    const val API_BASE_URL = "https://swsqirdcmxotncibmgeb.supabase.co"
    const val SUPABASE_ANON_KEY = "your-supabase-anon-key-here"
    const val BACKEND_URL = "https://your-backend-api.com"
    
    // Feature flags
    const val ENABLE_CRISIS_DETECTION = true
    const val ENABLE_ANALYTICS = false
    const val ENABLE_PUSH_NOTIFICATIONS = false
    
    // Debug settings
    const val LOG_HTTP_REQUESTS = true
    const val ENABLE_NETWORK_INSPECTOR = true
    
    // Performance settings
    const val ENABLE_PERFORMANCE_MONITORING = false
    const val ENABLE_CRASH_REPORTING = false
    
    // Logging level
    const val LOG_LEVEL = "DEBUG"
}
