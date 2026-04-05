package com.drmindit.android.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM cached_sessions ORDER BY cachedAt DESC LIMIT 5")
    fun getCachedSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM cached_sessions WHERE id = :id")
    suspend fun getSessionById(id: String): SessionEntity?

    @Query("SELECT * FROM cached_sessions WHERE isOfflineAvailable = 1")
    suspend fun getOfflineAvailableSessions(): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<SessionEntity>)

    @Query("DELETE FROM cached_sessions WHERE cachedAt < :timestamp")
    suspend fun deleteOldSessions(timestamp: Long)

    @Query("DELETE FROM cached_sessions")
    suspend fun clearAllSessions()

    @Query("UPDATE cached_sessions SET isOfflineAvailable = 1, localAudioPath = :localPath WHERE id = :sessionId")
    suspend fun markAsOfflineAvailable(sessionId: String, localPath: String)
}
