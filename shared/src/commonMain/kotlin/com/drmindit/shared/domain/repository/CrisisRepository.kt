package com.drmindit.shared.domain.repository

import com.drmindit.shared.domain.model.CrisisEvent
import com.drmindit.shared.domain.model.EmergencyHelpline
import kotlinx.coroutines.flow.Flow

interface CrisisRepository {
    suspend fun logCrisisEvent(event: CrisisEvent): Result<Unit>
    suspend fun getCrisisHistory(userId: String, limit: Int = 50): Result<List<CrisisEvent>>
    suspend fun getEmergencyHelplines(country: String? = null): Result<List<EmergencyHelpline>>
    suspend fun reportEscalation(eventId: String, notes: String): Result<Unit>
    fun observeActiveCrisisEvents(userId: String): Flow<CrisisEvent>
    suspend fun updateCrisisStatus(eventId: String, resolved: Boolean): Result<Unit>
    suspend fun getCrisisStatistics(userId: String): Result<Map<String, Int>>
}
