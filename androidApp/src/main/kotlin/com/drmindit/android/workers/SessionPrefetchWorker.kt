package com.drmindit.android.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.drmindit.android.data.database.SessionDao
import com.drmindit.android.data.database.SessionEntity
import com.drmindit.android.data.repository.SessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class SessionPrefetchWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var sessionDao: SessionDao

    override suspend fun doWork(): Result {
        return try {
            // Fetch latest sessions from repository
            val sessions = sessionRepository.getLatestSessions(5).first()
            
            // Convert to entities and cache in database
            val sessionEntities = sessions.map { session ->
                SessionEntity(
                    id = session.id,
                    title = session.title,
                    instructor = session.instructor,
                    duration = session.duration,
                    audioUrl = session.audioUrl,
                    imageUrl = session.imageUrl,
                    category = session.category,
                    description = session.description,
                    isOfflineAvailable = false // Will be marked true when audio is downloaded
                )
            }
            
            // Insert into database
            sessionDao.insertSessions(sessionEntities)
            
            // Clean up old cached sessions (older than 7 days)
            val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            sessionDao.deleteOldSessions(weekAgo)
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
