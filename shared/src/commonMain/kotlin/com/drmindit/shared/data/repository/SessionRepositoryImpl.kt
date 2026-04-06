package com.drmindit.shared.data.repository

import com.drmindit.shared.domain.model.*
import com.drmindit.shared.domain.repository.SessionRepository
import kotlinx.coroutines.flow.*

class SessionRepositoryImpl : SessionRepository {
    
    override suspend fun getSessions(category: SessionCategory?): Result<List<Session>> {
        // Mock implementation
        return Result.success(listOf(
            Session(
                id = "session1",
                title = "Breathing Meditation",
                description = "Basic breathing exercise",
                instructor = "Dr. Smith",
                duration = 600,
                audioUrl = "https://example.com/audio.mp3",
                imageUrl = "https://example.com/image.jpg",
                category = SessionCategory.MINDFULNESS,
                tags = listOf("breathing", "relaxation"),
                rating = 4.5f,
                difficulty = Difficulty.BEGINNER
            )
        ))
    }
    
    override suspend fun getSessionById(sessionId: String): Result<Session?> {
        return getSessions().map { sessions -> sessions.find { it.id == sessionId } }
    }
    
    override suspend fun searchSessions(query: String): Result<List<Session>> {
        return getSessions().map { sessions ->
            sessions.filter { session ->
                session.title.contains(query, ignoreCase = true) ||
                session.description.contains(query, ignoreCase = true) ||
                session.tags.any { it.contains(query, ignoreCase = true) }
            }
        }
    }
    
    override suspend fun getSessionOfTheDay(): Result<Session> {
        return getSessions().map { sessions -> sessions.first() }
    }
    
    override suspend fun toggleFavorite(sessionId: String): Result<Unit> {
        // Mock implementation
        return Result.success(Unit)
    }
    
    override suspend fun getFavoriteSessions(): Result<List<Session>> {
        // Mock implementation - return empty list since isFavorite is not in Session model
        return Result.success(emptyList())
    }
    
    override suspend fun updateSessionProgress(progress: SessionProgress): Result<Unit> {
        // Mock implementation
        return Result.success(Unit)
    }
    
    override suspend fun getSessionProgress(sessionId: String, userId: String): Result<SessionProgress?> {
        val progress = SessionProgress(
            sessionId = sessionId,
            userId = userId,
            completedDuration = 300,
            totalDuration = 600,
            isCompleted = false,
            completionDate = null
        )
        return Result.success(progress)
    }
    
    override fun observeSessionProgress(sessionId: String, userId: String): Flow<SessionProgress?> {
        return flowOf(
            SessionProgress(
                sessionId = sessionId,
                userId = userId,
                completedDuration = 300,
                totalDuration = 600,
                isCompleted = false,
                completionDate = null
            )
        )
    }
    
    override fun getLatestSessions(limit: Int): Flow<List<Session>> {
        return flow {
            emit(listOf(
                Session(
                    id = "latest1",
                    title = "Latest Session",
                    description = "Most recent session",
                    instructor = "Dr. Smith",
                    duration = 600,
                    audioUrl = "https://example.com/audio.mp3",
                    imageUrl = "https://example.com/image.jpg",
                    category = SessionCategory.MINDFULNESS,
                    tags = listOf("latest"),
                    rating = 4.5f,
                    difficulty = Difficulty.BEGINNER
                )
            ))
        }
    }
}
