package com.drmindit.android.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.drmindit.shared.domain.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of ChatRemoteDataSource
 */
@Singleton
class FirebaseChatDataSource @Inject constructor() {
    
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val chatSessionsCollection = firestore.collection("chat_sessions")
    private val messagesCollection = firestore.collection("chat_messages")
    private val crisisEventsCollection = firestore.collection("crisis_events")
    
    init {
        // Enable offline persistence
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }
    
    /**
     * Get current authenticated user
     */
    private fun getCurrentUser(): String? {
        return auth.currentUser?.uid
    }
    
    /**
     * Create a new chat session
     */
    suspend fun createChatSession(userId: String): Result<ChatSession> {
        return try {
            val sessionId = chatSessionsCollection.document().id
            val session = ChatSession(
                id = sessionId,
                userId = userId,
                title = "Session ${System.currentTimeMillis()}",
                messages = emptyList(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isActive = true,
                moodTags = emptyList(),
                summary = null
            )
            
            val sessionData = mapOf(
                "id" to session.id,
                "userId" to session.userId,
                "title" to session.title,
                "createdAt" to session.createdAt,
                "updatedAt" to session.updatedAt,
                "isActive" to session.isActive,
                "moodTags" to session.moodTags,
                "summary" to session.summary
            )
            
            chatSessionsCollection.document(sessionId).set(sessionData).await()
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get active chat session for user
     */
    suspend fun getActiveChatSession(userId: String): Result<ChatSession?> {
        return try {
            val query = chatSessionsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()
            
            val session = query.documents.firstOrNull()?.let { doc ->
                doc.toObject(ChatSession::class.java)?.copy(id = doc.id)
            }
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Add message to chat session
     */
    suspend fun addMessage(sessionId: String, message: ChatMessage): Result<Unit> {
        return try {
            val messageData = mapOf(
                "id" to message.id,
                "sessionId" to sessionId,
                "text" to message.text,
                "sender" to message.sender.name,
                "timestamp" to message.timestamp,
                "messageType" to message.messageType.name,
                "metadata" to message.metadata,
                "isRead" to message.isRead
            )
            
            messagesCollection.document(message.id).set(messageData).await()
            
            // Update session timestamp
            chatSessionsCollection.document(sessionId)
                .update("updatedAt", System.currentTimeMillis())
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get messages for a session
     */
    suspend fun getMessages(sessionId: String, limit: Int = 50): Result<List<ChatMessage>> {
        return try {
            val query = messagesCollection
                .whereEqualTo("sessionId", sessionId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val messages = query.documents.mapNotNull { doc ->
                doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
            }
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Observe messages in real-time
     */
    fun observeMessages(sessionId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = messagesCollection
            .whereEqualTo("sessionId", sessionId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Report crisis event
     */
    suspend fun reportCrisisEvent(crisisEvent: CrisisEvent): Result<Unit> {
        return try {
            val eventData = mapOf(
                "id" to crisisEvent.id,
                "userId" to crisisEvent.userId,
                "sessionId" to crisisEvent.sessionId,
                "message" to crisisEvent.message,
                "riskLevel" to crisisEvent.riskLevel.name,
                "timestamp" to crisisEvent.timestamp,
                "resolved" to crisisEvent.resolved,
                "escalated" to crisisEvent.escalated,
                "notes" to crisisEvent.notes,
                "followUpRequired" to crisisEvent.followUpRequired,
                "followUpTimestamp" to crisisEvent.followUpTimestamp
            )
            
            crisisEventsCollection.document(crisisEvent.id).set(eventData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get crisis events for user
     */
    suspend fun getCrisisEvents(userId: String): Result<List<CrisisEvent>> {
        return try {
            val query = crisisEventsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val events = query.documents.mapNotNull { doc ->
                doc.toObject(CrisisEvent::class.java)?.copy(id = doc.id)
            }
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update chat session
     */
    suspend fun updateChatSession(session: ChatSession): Result<Unit> {
        return try {
            val sessionData = mapOf(
                "title" to session.title,
                "updatedAt" to session.updatedAt,
                "isActive" to session.isActive,
                "moodTags" to session.moodTags,
                "summary" to session.summary
            )
            
            chatSessionsCollection.document(session.id)
                .update(sessionData)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete chat session
     */
    suspend fun deleteChatSession(sessionId: String): Result<Unit> {
        return try {
            // Delete session
            chatSessionsCollection.document(sessionId).delete().await()
            
            // Delete all messages in the session
            val messagesQuery = messagesCollection
                .whereEqualTo("sessionId", sessionId)
                .get()
                .await()
            
            messagesQuery.documents.forEach { doc ->
                messagesCollection.document(doc.id).delete()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search messages
     */
    suspend fun searchMessages(userId: String, query: String): Result<List<ChatMessage>> {
        return try {
            // First get user's sessions
            val sessionsQuery = chatSessionsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val sessionIds = sessionsQuery.documents.map { it.id }
            
            // Then search messages in those sessions
            val messagesQuery = messagesCollection
                .whereIn("sessionId", sessionIds)
                .whereGreaterThanOrEqualTo("text", query)
                .whereLessThanOrEqualTo("text", query + "\uf8ff")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()
            
            val messages = messagesQuery.documents.mapNotNull { doc ->
                doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
            }
            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
