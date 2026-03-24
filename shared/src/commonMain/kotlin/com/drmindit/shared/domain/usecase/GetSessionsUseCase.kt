package com.drmindit.shared.domain.usecase

import com.drmindit.shared.domain.model.SessionCategory
import com.drmindit.shared.domain.repository.SessionRepository

class GetSessionsUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(category: SessionCategory? = null): Result<List<com.drmindit.shared.domain.model.Session>> {
        return sessionRepository.getSessions(category)
    }
}

class GetSessionOfTheDayUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(): Result<com.drmindit.shared.domain.model.Session> {
        return sessionRepository.getSessionOfTheDay()
    }
}

class SearchSessionsUseCase(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(query: String): Result<List<com.drmindit.shared.domain.model.Session>> {
        return sessionRepository.searchSessions(query)
    }
}
