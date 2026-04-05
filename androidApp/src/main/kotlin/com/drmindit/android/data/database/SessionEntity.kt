package com.drmindit.android.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_sessions")
data class SessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val instructor: String,
    val duration: Int,
    val audioUrl: String,
    val imageUrl: String?,
    val category: String,
    val description: String?,
    val isOfflineAvailable: Boolean = false,
    val localAudioPath: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)
