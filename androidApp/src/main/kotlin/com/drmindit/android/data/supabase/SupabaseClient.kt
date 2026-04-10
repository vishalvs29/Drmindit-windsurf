package com.drmindit.android.data.supabase

import android.content.Context
import com.drmindit.android.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Supabase client configuration for DrMindit
 * Uses the new Supabase project: https://supabase.com/dashboard/project/nlheesoshtczdhsqzjid
 */
object SupabaseClient {
    
    private lateinit var supabaseClient: SupabaseClient
    
    fun initialize(context: Context) {
        supabaseClient = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            // Configure authentication
            install(Auth)
            
            // Configure Postgrest (database)
            install(Postgrest)
        }
    }
    
    fun getClient(): SupabaseClient {
        if (!::supabaseClient.isInitialized) {
            throw IllegalStateException("SupabaseClient must be initialized before use")
        }
        return supabaseClient
    }
    
    /**
     * Get the auth instance
     */
    fun auth() = getClient().auth
    
    /**
     * Get the database instance
     */
    fun database() = getClient().postgrest
}
