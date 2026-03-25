package com.drmindit.android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.room.*
import com.drmindit.shared.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions WHERE userId = :userId ORDER BY updatedAt DESC")
    suspend fun getAllChatSessions(userId: String): List<ChatSessionEntity>
    
    @Query("SELECT * FROM chat_sessions WHERE userId = :userId AND isActive = 1 LIMIT 1")
    suspend fun getActiveChatSession(userId: String): ChatSessionEntity?
    
    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
    suspend fun getChatSession(sessionId: String): ChatSessionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatSession(session: ChatSessionEntity)
    
    @Update
    suspend fun updateChatSession(session: ChatSessionEntity)
    
    @Delete
    suspend fun deleteChatSession(session: ChatSessionEntity)
    
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getMessages(sessionId: String, limit: Int): List<ChatMessageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)
    
    @Update
    suspend fun updateMessage(message: ChatMessageEntity)
    
    @Delete
    suspend fun deleteMessage(message: ChatMessageEntity)
    
    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId AND id = :messageId")
    suspend fun deleteMessageById(sessionId: String, messageId: String)
    
    @Query("""
        SELECT m.* FROM chat_messages m 
        INNER JOIN chat_sessions s ON m.sessionId = s.id 
        WHERE s.userId = :userId AND m.text LIKE '%' || :query || '%'
        ORDER BY m.timestamp DESC
    """)
    suspend fun searchMessages(userId: String, query: String): List<ChatMessageEntity>
}

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean = true,
    val moodTags: String = "", // JSON serialized list
    val summary: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val text: String,
    val sender: String, // Enum serialized as string
    val timestamp: Long,
    val messageType: String, // Enum serialized as string
    val metadata: String = "", // JSON serialized
    val isRead: Boolean = false,
    val sessionIdRef: String // Foreign key reference
)

@Database(
    entities = [ChatSessionEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}

class ChatLocalDataSourceImpl(
    private val context: Context,
    private val database: ChatDatabase,
    private val dataStore: DataStore<Preferences>
) : com.drmindit.shared.domain.repository.ChatLocalDataSource {
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true 
    }
    
    override suspend fun saveChatSession(session: ChatSession) {
        val entity = ChatSessionEntity(
            id = session.id,
            userId = session.userId,
            title = session.title,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt,
            isActive = session.isActive,
            moodTags = json.encodeToString(session.moodTags),
            summary = session.summary
        )
        database.chatDao().insertChatSession(entity)
    }
    
    override suspend fun getChatSession(sessionId: String): ChatSession? {
        val entity = database.chatDao().getChatSession(sessionId)
        return entity?.toChatSession()
    }
    
    override suspend fun getActiveChatSession(userId: String): ChatSession? {
        val entity = database.chatDao().getActiveChatSession(userId)
        return entity?.toChatSession()
    }
    
    override suspend fun updateChatSession(session: ChatSession) {
        val entity = ChatSessionEntity(
            id = session.id,
            userId = session.userId,
            title = session.title,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt,
            isActive = session.isActive,
            moodTags = json.encodeToString(session.moodTags),
            summary = session.summary
        )
        database.chatDao().updateChatSession(entity)
    }
    
    override suspend fun deleteChatSession(sessionId: String) {
        val entity = database.chatDao().getChatSession(sessionId)
        if (entity != null) {
            database.chatDao().deleteChatSession(entity)
        }
    }
    
    override suspend fun getAllChatSessions(userId: String): List<ChatSession> {
        val entities = database.chatDao().getAllChatSessions(userId)
        return entities.mapNotNull { it.toChatSession() }
    }
    
    override suspend fun addMessage(sessionId: String, message: ChatMessage) {
        val entity = ChatMessageEntity(
            id = message.id,
            sessionId = sessionId,
            text = message.text,
            sender = message.sender.name,
            timestamp = message.timestamp,
            messageType = message.messageType.name,
            metadata = json.encodeToString(message.metadata),
            isRead = message.isRead,
            sessionIdRef = sessionId
        )
        database.chatDao().insertMessage(entity)
    }
    
    override suspend fun updateMessage(message: ChatMessage) {
        val entity = ChatMessageEntity(
            id = message.id,
            sessionId = message.sessionId ?: "",
            text = message.text,
            sender = message.sender.name,
            timestamp = message.timestamp,
            messageType = message.messageType.name,
            metadata = json.encodeToString(message.metadata),
            isRead = message.isRead,
            sessionIdRef = message.sessionId ?: ""
        )
        database.chatDao().updateMessage(entity)
    }
    
    override suspend fun deleteMessage(sessionId: String, messageId: String) {
        database.chatDao().deleteMessageById(sessionId, messageId)
    }
    
    override suspend fun getMessages(sessionId: String, limit: Int): List<ChatMessage> {
        val entities = database.chatDao().getMessages(sessionId, limit)
        return entities.mapNotNull { it.toChatMessage() }
    }
    
    override suspend fun searchMessages(userId: String, query: String): List<ChatMessage> {
        val entities = database.chatDao().searchMessages(userId, query)
        return entities.mapNotNull { it.toChatMessage() }
    }
    
    override suspend fun saveChatPreferences(preferences: ChatPreferences) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("chat_preferences_${preferences.userId}")] = 
                json.encodeToString(preferences)
        }
    }
    
    override suspend fun getChatPreferences(userId: String): ChatPreferences? {
        return try {
            val jsonString = dataStore.data.map { 
                it[stringPreferencesKey("chat_preferences_$userId")] 
            }.value
            jsonString?.let { json.decodeFromString<ChatPreferences>(it) }
        } catch (e: Exception) {
            null
        }
    }
    
    override fun observeChatSession(sessionId: String): Flow<ChatSession?> {
        return flow {
            // In a real implementation, you'd use Room's Flow return type
            // For now, emit the current state
            emit(getChatSession(sessionId))
        }
    }
    
    override fun observeMessages(sessionId: String): Flow<List<ChatMessage>> {
        return flow {
            // In a real implementation, you'd use Room's Flow return type
            // For now, emit the current state
            emit(getMessages(sessionId, 50))
        }
    }
    
    override fun observeTypingIndicator(sessionId: String): Flow<Boolean> {
        return flow {
            // In a real implementation, you'd have a typing indicator table
            // For now, always emit false
            emit(false)
        }
    }
    
    // Extension functions for entity conversion
    private fun ChatSessionEntity.toChatSession(): ChatSession? {
        return try {
            val moodTags = if (moodTags.isNotEmpty()) {
                json.decodeFromString<List<MoodCategory>>(moodTags)
            } else {
                emptyList()
            }
            
            // Get messages for this session
            val messageEntities = database.chatDao().getMessages(id, Int.MAX_VALUE)
            val messages = messageEntities.mapNotNull { it.toChatMessage() }
            
            ChatSession(
                id = id,
                userId = userId,
                title = title,
                messages = messages,
                createdAt = createdAt,
                updatedAt = updatedAt,
                isActive = isActive,
                moodTags = moodTags,
                summary = summary
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun ChatMessageEntity.toChatMessage(): ChatMessage? {
        return try {
            val sender = try {
                MessageSender.valueOf(sender)
            } catch (e: IllegalArgumentException) {
                MessageSender.USER
            }
            
            val messageType = try {
                MessageType.valueOf(messageType)
            } catch (e: IllegalArgumentException) {
                MessageType.TEXT
            }
            
            val metadata = if (this.metadata.isNotEmpty()) {
                json.decodeFromString<MessageMetadata>(this.metadata)
            } else {
                null
            }
            
            ChatMessage(
                id = id,
                text = text,
                sender = sender,
                timestamp = timestamp,
                messageType = messageType,
                metadata = metadata,
                isRead = isRead,
                sessionId = sessionId
            )
        } catch (e: Exception) {
            null
        }
    }
}

// Database builder
fun provideChatDatabase(context: Context): ChatDatabase {
    return Room.databaseBuilder(
        context,
        ChatDatabase::class.java,
        "chat_database"
    ).build()
}

// DataStore provider
fun provideChatDataStore(context: Context): DataStore<Preferences> {
    return androidx.datastore.preferences.preferencesDataStore(
        name = "chat_preferences"
    )
}

// Chat preferences keys
object ChatPreferencesKeys {
    val CHAT_PREFERENCES_PREFIX = "chat_preferences_"
    val TYPING_INDICATOR_PREFIX = "typing_indicator_"
    val LAST_MESSAGE_PREFIX = "last_message_"
}

// Typing indicator management (simplified)
class TypingIndicatorManager(
    private val dataStore: DataStore<Preferences>
) {
    suspend fun setTyping(sessionId: String, userId: String, isTyping: Boolean) {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("${ChatPreferencesKeys.TYPING_INDICATOR_PREFIX}${sessionId}_${userId}")] = isTyping
        }
    }
    
    fun observeTyping(sessionId: String, userId: String): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[booleanPreferencesKey("${ChatPreferencesKeys.TYPING_INDICATOR_PREFIX}${sessionId}_${userId}")] ?: false
        }
    }
    
    suspend fun clearTyping(sessionId: String) {
        // Clear all typing indicators for a session
        // Implementation depends on your specific needs
    }
}

// Chat backup and restore
class ChatBackupManager(
    private val chatDao: ChatDao,
    private val json: Json
) {
    suspend fun exportChatData(userId: String): String? {
        return try {
            val sessions = chatDao.getAllChatSessions(userId)
            val exportData = ChatExportData(
                sessions = sessions.map { it.toExportSession() },
                exportDate = System.currentTimeMillis(),
                version = 1
            )
            json.encodeToString(exportData)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun importChatData(userId: String, data: String): Boolean {
        return try {
            val exportData = json.decodeFromString<ChatExportData>(data)
            exportData.sessions.forEach { exportSession ->
                val entity = exportSession.toChatSessionEntity(userId)
                chatDao.insertChatSession(entity)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun ChatSessionEntity.toExportSession(): ExportChatSession {
        return ExportChatSession(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isActive = isActive,
            moodTags = moodTags,
            summary = summary
        )
    }
    
    private fun ExportChatSession.toChatSessionEntity(userId: String): ChatSessionEntity {
        return ChatSessionEntity(
            id = id,
            userId = userId,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isActive = isActive,
            moodTags = moodTags,
            summary = summary
        )
    }
}

data class ChatExportData(
    val sessions: List<ExportChatSession>,
    val exportDate: Long,
    val version: Int
)

data class ExportChatSession(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean,
    val moodTags: String,
    val summary: String?
)
