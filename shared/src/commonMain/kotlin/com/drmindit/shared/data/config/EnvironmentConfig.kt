package com.drmindit.shared.data.config

import kotlin.properties.Delegates

object EnvironmentConfig {
    private const val DEFAULT_SUPABASE_URL = "https://placeholder-project.supabase.co"
    private const val DEFAULT_SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.placeholder.anon.key"

    // Environment-based configuration
    val environment: Environment by lazy {
        when (System.getProperty("app.env") ?: "development") {
            "production" -> Environment.PRODUCTION
            "staging" -> Environment.STAGING
            else -> Environment.DEVELOPMENT
        }
    }

    val supabaseUrl: String by lazy {
        System.getProperty("supabase.url") ?: DEFAULT_SUPABASE_URL
    }

    val supabaseAnonKey: String by lazy {
        System.getProperty("supabase.anon_key") ?: DEFAULT_SUPABASE_ANON_KEY
    }

    val supabaseServiceKey: String by lazy {
        System.getProperty("supabase.service_key") ?: ""
    }

    val isDebugMode: Boolean by lazy {
        environment == Environment.DEVELOPMENT
    }

    val logLevel: LogLevel by lazy {
        when (environment) {
            Environment.PRODUCTION -> LogLevel.ERROR
            Environment.STAGING -> LogLevel.INFO
            Environment.DEVELOPMENT -> LogLevel.DEBUG
        }
    }

    val apiTimeout: Long by lazy {
        when (environment) {
            Environment.PRODUCTION -> 30000L
            Environment.STAGING -> 30000L
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

    fun getBaseUrl(): String = supabaseUrl
    fun getAnonKey(): String = supabaseAnonKey
    fun getServiceKey(): String = supabaseServiceKey
}
