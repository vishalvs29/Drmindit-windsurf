package com.drmindit.android.usecase

import com.drmindit.shared.domain.model.Difficulty
import com.drmindit.shared.domain.model.Session
import com.drmindit.shared.domain.model.SessionCategory
import com.drmindit.shared.domain.repository.SessionRepository
import com.drmindit.shared.domain.usecase.GetSessionsUseCase
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetSessionsUseCaseTest {
    
    private lateinit var sessionRepository: SessionRepository
    private lateinit var getSessionsUseCase: GetSessionsUseCase
    
    @Before
    fun setup() {
        sessionRepository = mockk()
        getSessionsUseCase = GetSessionsUseCase(sessionRepository)
    }
    
    @Test
    fun `invoke returns sessions successfully`() = runBlocking {
        // Given
        val expectedSessions = listOf(
            Session(
                id = "session1",
                title = "Meditation for Beginners",
                description = "A gentle introduction to meditation",
                instructor = "Dr. Smith",
                duration = 10,
                audioUrl = "https://example.com/audio1.mp3",
                imageUrl = "https://example.com/thumb1.jpg",
                category = SessionCategory.MINDFULNESS,
                tags = listOf("meditation", "beginners"),
                rating = 4.5f,
                totalRatings = 100,
                difficulty = Difficulty.BEGINNER
            ),
            Session(
                id = "session2",
                title = "Sleep Stories",
                description = "Calming stories for better sleep",
                instructor = "Dr. Johnson",
                duration = 15,
                audioUrl = "https://example.com/audio2.mp3",
                imageUrl = "https://example.com/thumb2.jpg",
                category = SessionCategory.SLEEP,
                tags = listOf("sleep", "stories"),
                rating = 4.8f,
                totalRatings = 200,
                difficulty = Difficulty.BEGINNER
            )
        )
        
        coEvery { sessionRepository.getSessions(any()) } returns Result.success(expectedSessions)
        
        // When
        val result = getSessionsUseCase()
        
        // Then
        assertTrue(result.isSuccess)
        val sessions = result.getOrNull()
        assertEquals(expectedSessions.size, sessions?.size)
        assertEquals(expectedSessions[0].id, sessions?.get(0)?.id)
        assertEquals(expectedSessions[1].title, sessions?.get(1)?.title)
    }
    
    @Test
    fun `invoke handles repository error`() = runBlocking {
        // Given
        val errorMessage = "Network error"
        coEvery { sessionRepository.getSessions(any()) } returns Result.failure(RuntimeException(errorMessage))
        
        // When
        val result = getSessionsUseCase()
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(errorMessage, result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `invoke returns empty list when no sessions available`() = runBlocking {
        // Given
        val emptySessions = emptyList<Session>()
        coEvery { sessionRepository.getSessions(any()) } returns Result.success(emptySessions)
        
        // When
        val result = getSessionsUseCase()
        
        // Then
        assertTrue(result.isSuccess)
        val sessions = result.getOrNull()
        assertEquals(0, sessions?.size)
    }
}
