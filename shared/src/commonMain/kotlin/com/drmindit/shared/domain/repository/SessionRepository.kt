package com.drmindit.shared.domain.repository

import com.drmindit.shared.domain.model.Session
import com.drmindit.shared.domain.model.SessionCategory
import com.drmindit.shared.domain.model.SessionProgress
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    suspend fun getSessions(category: SessionCategory? = null): Result<List<Session>>
    suspend fun getSessionById(sessionId: String): Result<Session?>
    suspend fun searchSessions(query: String): Result<List<Session>>
    suspend fun getSessionOfTheDay(): Result<Session>
    suspend fun toggleFavorite(sessionId: String): Result<Unit>
    suspend fun getFavoriteSessions(): Result<List<Session>>
    suspend fun updateSessionProgress(progress: SessionProgress): Result<Unit>
    suspend fun getSessionProgress(sessionId: String, userId: String): Result<SessionProgress?>
    fun observeSessionProgress(sessionId: String, userId: String): Flow<SessionProgress?>
}
