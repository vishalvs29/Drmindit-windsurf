package com.drmindit.android.domain.model

/**
 * Simple user model for authentication and profile
 */
data class User(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val avatar: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = "$firstName $lastName"
    
    val initials: String
        get() = "${firstName.firstOrNull()}${lastName.firstOrNull()}".uppercase()
}
