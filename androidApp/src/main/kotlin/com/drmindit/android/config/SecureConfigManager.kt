package com.drmindit.android.config

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.drmindit.android.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure Configuration Manager
 * Handles secure storage and retrieval of sensitive configuration
 */
@Singleton
class SecureConfigManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val encryptedPrefs: EncryptedSharedPreferences
    
    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setKeySize(MasterKey.KEY_SIZE_256)
            .build()
        
        encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            "secure_config_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Get Supabase URL securely
     */
    fun getSupabaseUrl(): String {
        return System.getenv("SUPABASE_URL") 
            ?: BuildConfig.SUPABASE_URL
            ?: encryptedPrefs.getString("supabase_url", "")
            ?: "https://your-project.supabase.co"
    }
    
    /**
     * Get Supabase Anon Key securely
     */
    fun getSupabaseAnonKey(): String {
        return System.getenv("SUPABASE_ANON_KEY")
            ?: BuildConfig.SUPABASE_ANON_KEY
            ?: encryptedPrefs.getString("supabase_anon_key", "")
            ?: "your-anon-key-here"
    }
    
    /**
     * Get Firebase Project ID securely
     */
    fun getFirebaseProjectId(): String {
        return System.getenv("FIREBASE_PROJECT_ID")
            ?: encryptedPrefs.getString("firebase_project_id", "")
            ?: "your-firebase-project-id"
    }
    
    /**
     * Get Google Web Client ID securely
     */
    fun getGoogleWebClientId(): String {
        return System.getenv("GOOGLE_WEB_CLIENT_ID")
            ?: encryptedPrefs.getString("google_web_client_id", "")
            ?: "your-web-client-id.apps.googleusercontent.com"
    }
    
    /**
     * Store sensitive configuration securely
     */
    fun storeSecureConfig(key: String, value: String) {
        encryptedPrefs.edit()
            .putString(key, value)
            .apply()
    }
    
    /**
     * Get sensitive configuration securely
     */
    fun getSecureConfig(key: String, defaultValue: String = ""): String {
        return System.getenv(key.uppercase())
            ?: encryptedPrefs.getString(key, defaultValue)
            ?: defaultValue
    }
    
    /**
     * Check if production environment
     */
    fun isProduction(): Boolean {
        return (System.getenv("APP_ENV") ?: "development") == "production"
    }
    
    /**
     * Check if debug mode
     */
    fun isDebugMode(): Boolean {
        return (System.getenv("APP_ENV") ?: "development") != "production"
    }
    
    /**
     * Get API timeout configuration
     */
    fun getApiTimeout(): Long {
        return when (System.getenv("APP_ENV") ?: "development") {
            "production" -> 30000L
            "staging" -> 20000L
            else -> 15000L
        }
    }
    
    /**
     * Get Sentry DSN securely
     */
    fun getSentryDsn(): String? {
        return if (isProduction()) {
            System.getenv("SENTRY_DSN")
                ?: encryptedPrefs.getString("sentry_dsn", null)
        } else {
            null // No error reporting in development
        }
    }
    
    /**
     * Clear all secure configuration (for testing/reset)
     */
    fun clearSecureConfig() {
        encryptedPrefs.edit().clear().apply()
    }
    
    /**
     * Validate required configuration
     */
    fun validateConfiguration(): List<String> {
        val errors = mutableListOf<String>()
        
        if (getSupabaseUrl().contains("your-project")) {
            errors.add("Supabase URL is not configured")
        }
        
        if (getSupabaseAnonKey().contains("your-anon-key")) {
            errors.add("Supabase Anon Key is not configured")
        }
        
        if (isProduction() && getFirebaseProjectId().contains("your-firebase")) {
            errors.add("Firebase Project ID is required for production")
        }
        
        return errors
    }
}
