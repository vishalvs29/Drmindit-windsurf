package com.drmindit.shared.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

object SupabaseClient {
    
    private const val SUPABASE_URL = "https://swsqirdcmxotncibmgeb.supabase.co"
    private const val SUPABASE_ANON_KEY = "your-supabase-anon-key-here"
    
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
    
    // Auth instance
    val auth: Auth = client.auth
    
    // Database instance
    val database: Postgrest = client.postgrest
    
    // Storage instance
    val storage: Storage = client.storage
}

// Extension for easy table access
fun Postgrest.from(table: String) = this.from(table)
