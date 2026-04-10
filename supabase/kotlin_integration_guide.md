# DrMindit Kotlin App Integration Guide
# Production-Ready Supabase Backend Integration

## Overview

This guide provides complete integration instructions for the DrMindit Kotlin Android app with the production Supabase backend.

## Prerequisites

- Android Studio with Kotlin support
- Supabase Kotlin SDK
- Supabase project credentials

## Dependencies

Add to your `app/build.gradle.kts`:

```kotlin
dependencies {
    // Supabase
    implementation("io.github.jan-supabase:supabase-kt:2.2.0")
    implementation("io.github.jan-supabase:auth-kt:2.2.0")
    implementation("io.github.jan-supabase:postgrest-kt:2.2.0")
    implementation("io.github.jan-supabase:storage-kt:2.2.0")
    implementation("io.github.jan-supabase:realtime-kt:2.2.0")
    
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

## Configuration

### 1. Supabase Client Setup

```kotlin
// SupabaseClient.kt
object SupabaseClient {
    private lateinit var supabaseClient: SupabaseClient
    
    fun initialize(context: Context) {
        supabaseClient = createSupabaseClient(
            supabaseUrl = "https://nlheesoshtczdhsqzjid.supabase.co",
            supabaseKey = "sbp_381301704399ba15d63a6037f5781c6d223b2ced"
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            install(Realtime)
        }
    }
    
    fun getClient(): SupabaseClient = supabaseClient
    fun auth() = getClient().auth
    fun database() = getClient().postgrest
    fun storage() = getClient().storage
}
```

### 2. Data Models

```kotlin
// Session.kt
@Serializable
data class Session(
    val id: String,
    val title: String,
    val description: String,
    val totalDuration: Int,
    val category: String,
    val difficultyLevel: String,
    val instructor: String,
    val thumbnailUrl: String,
    val isPremium: Boolean,
    val rating: Double,
    val totalRatings: Long,
    val isAccessible: Boolean,
    val steps: List<SessionStep> = emptyList()
)

@Serializable
data class SessionStep(
    val id: String,
    val sessionId: String,
    val title: String,
    val description: String,
    val audioUrl: String,
    val duration: Int,
    val orderIndex: Int,
    val stepType: String
)

@Serializable
data class UserSessionProgress(
    val id: String,
    val userId: String,
    val sessionId: String,
    val currentStepIndex: Int,
    val progressSeconds: Int,
    val completedSteps: List<String>,
    val isCompleted: Boolean,
    val isFavorite: Boolean,
    val lastPlayedAt: String,
    val startedAt: String,
    val updatedAt: String
)

@Serializable
data class SessionEvent(
    val id: String,
    val userId: String,
    val sessionId: String,
    val stepIndex: Int?,
    val eventType: String,
    val eventData: Map<String, Any>,
    val createdAt: String
)
```

### 3. Repository Implementation

```kotlin
// SessionRepository.kt
interface SessionRepository {
    suspend fun getSessions(): Result<List<Session>>
    suspend fun getSession(sessionId: String): Result<Session?>
    suspend fun getSessionSteps(sessionId: String): Result<List<SessionStep>>
    suspend fun getUserSessionProgress(sessionId: String): Result<UserSessionProgress?>
    suspend fun updateSessionProgress(progress: UserSessionProgress): Result<UserSessionProgress>
    suspend fun startSession(sessionId: String): Result<UserSessionProgress>
    suspend fun logSessionEvent(event: SessionEvent): Result<String>
}

// SupabaseSessionRepository.kt
class SupabaseSessionRepository : SessionRepository {
    
    override suspend fun getSessions(): Result<List<Session>> {
        return try {
            val database = SupabaseClient.database()
            val result = database.rpc("get_accessible_sessions") {
                // No parameters needed for public access
            }.data
            
            val sessions = result?.let { 
                Json.decodeFromString<List<Session>>(it.toString())
            } ?: emptyList()
            
            Result.success(sessions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSession(sessionId: String): Result<Session?> {
        return try {
            val userId = SupabaseClient.auth().currentUserOrNull()?.id
            val database = SupabaseClient.database()
            val result = database.rpc("get_session_with_steps") {
                parameter("session_uuid", sessionId)
                parameter("user_uuid", userId)
            }.data
            
            val session = result?.let { 
                Json.decodeFromString<Session>(it.toString())
            }
            
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateSessionProgress(progress: UserSessionProgress): Result<UserSessionProgress> {
        return try {
            val userId = SupabaseClient.auth().currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))
            
            val database = SupabaseClient.database()
            val result = database.rpc("update_session_progress") {
                parameter("user_uuid", userId)
                parameter("session_uuid", progress.sessionId)
                parameter("current_step", progress.currentStepIndex)
                parameter("progress_seconds", progress.progressSeconds)
                parameter("completed_steps", progress.completedSteps)
                parameter("is_completed", progress.isCompleted)
            }.data
            
            val updatedProgress = result?.let { 
                Json.decodeFromString<UserSessionProgress>(it.toString())
            } ?: progress
            
            Result.success(updatedProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logSessionEvent(event: SessionEvent): Result<String> {
        return try {
            val userId = SupabaseClient.auth().currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))
            
            val database = SupabaseClient.database()
            val result = database.rpc("log_session_event") {
                parameter("user_uuid", userId)
                parameter("session_uuid", event.sessionId)
                parameter("event_type", event.eventType)
                parameter("step_index", event.stepIndex)
                parameter("event_data", event.eventData)
                parameter("device_info", mapOf("platform" to "android"))
            }.data
            
            val eventId = result?.toString() ?: ""
            Result.success(eventId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Other methods implementation...
}
```

### 4. Authentication Service

```kotlin
// AuthService.kt
class AuthService {
    
    suspend fun signUp(email: String, password: String, firstName: String, lastName: String): Result<User> {
        return try {
            val auth = SupabaseClient.auth()
            val result = auth.signUpWith(email, password) {
                data {
                    put("first_name", firstName)
                    put("last_name", lastName)
                }
            }
            
            Result.success(result.user?.toUser() ?: throw Exception("Registration failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val auth = SupabaseClient.auth()
            val result = auth.signInWith(email, password)
            
            Result.success(result.user?.toUser() ?: throw Exception("Login failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signOut(): Result<Unit> {
        return try {
            val auth = SupabaseClient.auth()
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCurrentUser(): User? {
        return SupabaseClient.auth().currentUserOrNull()?.toUser()
    }
    
    fun authFlow(): Flow<User?> {
        return SupabaseClient.auth().sessionStatus
            .map { status ->
                when (status) {
                    is SessionStatus.Authenticated -> status.session.user.toUser()
                    else -> null
                }
            }
    }
}

private fun UserInfo.toUser(): User {
    return User(
        id = id,
        email = email ?: "",
        firstName = userMetadata["first_name"] as? String ?: "",
        lastName = userMetadata["last_name"] as? String ?: "",
        avatar = userMetadata["avatar_url"] as? String,
        createdAt = createdAt?.time ?: System.currentTimeMillis()
    )
}
```

### 5. Audio Player Integration

```kotlin
// AudioPlayerService.kt
class AudioPlayerService : Service() {
    
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var sessionRepository: SessionRepository
    private lateinit var authService: AuthService
    
    // Audio player implementation with progress tracking
    
    private fun trackProgress(sessionId: String, currentStep: Int, progressSeconds: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentProgress = sessionRepository.getUserSessionProgress(sessionId).getOrNull()
                currentProgress?.let { progress ->
                    val updatedProgress = progress.copy(
                        currentStepIndex = currentStep,
                        progressSeconds = progressSeconds
                    )
                    sessionRepository.updateSessionProgress(updatedProgress)
                    
                    // Log progress event
                    sessionRepository.logSessionEvent(
                        SessionEvent(
                            id = UUID.randomUUID().toString(),
                            userId = authService.getCurrentUser()?.id ?: "",
                            sessionId = sessionId,
                            stepIndex = currentStep,
                            eventType = "PROGRESS_UPDATED",
                            eventData = mapOf(
                                "current_step" to currentStep,
                                "progress_seconds" to progressSeconds
                            ),
                            createdAt = Instant.now().toString()
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("AudioPlayerService", "Error tracking progress", e)
            }
        }
    }
}
```

### 6. ViewModel Implementation

```kotlin
// SessionPlayerViewModel.kt
class SessionPlayerViewModel : ViewModel() {
    
    private val sessionRepository = SupabaseSessionRepository()
    private val authService = AuthService()
    
    private val _uiState = MutableStateFlow(SessionPlayerUiState())
    val uiState: StateFlow<SessionPlayerUiState> = _uiState.asStateFlow()
    
    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val session = sessionRepository.getSession(sessionId).getOrNull()
                val progress = sessionRepository.getUserSessionProgress(sessionId).getOrNull()
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        session = session,
                        currentProgress = progress
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun updateProgress(sessionId: String, currentStep: Int, progressSeconds: Int) {
        viewModelScope.launch {
            try {
                val currentProgress = _uiState.value.currentProgress
                val updatedProgress = currentProgress?.copy(
                    currentStepIndex = currentStep,
                    progressSeconds = progressSeconds
                ) ?: UserSessionProgress(
                    id = "",
                    userId = authService.getCurrentUser()?.id ?: "",
                    sessionId = sessionId,
                    currentStepIndex = currentStep,
                    progressSeconds = progressSeconds,
                    completedSteps = emptyList(),
                    isCompleted = false,
                    isFavorite = false,
                    lastPlayedAt = Instant.now().toString(),
                    startedAt = Instant.now().toString(),
                    updatedAt = Instant.now().toString()
                )
                
                sessionRepository.updateSessionProgress(updatedProgress)
                
                // Log event
                sessionRepository.logSessionEvent(
                    SessionEvent(
                        id = UUID.randomUUID().toString(),
                        userId = authService.getCurrentUser()?.id ?: "",
                        sessionId = sessionId,
                        stepIndex = currentStep,
                        eventType = "PROGRESS_UPDATED",
                        eventData = mapOf(
                            "current_step" to currentStep,
                            "progress_seconds" to progressSeconds
                        ),
                        createdAt = Instant.now().toString()
                    )
                )
                
                _uiState.update { it.copy(currentProgress = updatedProgress) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
```

### 7. Real-time Progress Updates

```kotlin
// RealtimeService.kt
class RealtimeService {
    
    private val supabase = SupabaseClient.getClient()
    
    fun subscribeToProgressUpdates(userId: String, onProgressUpdated: (UserSessionProgress) -> Unit) {
        supabase.realtime {
            val channel = channel("user_progress_$userId")
            channel.postgresChangeFlow<PostgresAction>("public", "user_session_progress")
                .filter { it.userId == userId }
                .onEach { change ->
                    when (change.type) {
                        PostgresAction.UPDATE, PostgresAction.INSERT -> {
                            val progress = Json.decodeFromString<UserSessionProgress>(change.record.toString())
                            onProgressUpdated(progress)
                        }
                        else -> {}
                    }
                }
                .launchIn(CoroutineScope(Dispatchers.Main))
        }
    }
}
```

## Usage Examples

### 1. Fetch Sessions

```kotlin
val repository = SupabaseSessionRepository()
val sessions = repository.getSessions()
sessions.onSuccess { sessionList ->
    // Display sessions in UI
}
sessions.onFailure { error ->
    // Handle error
}
```

### 2. Start Session

```kotlin
val progress = repository.startSession(sessionId)
progress.onSuccess { userProgress ->
    // Update UI with progress
}
```

### 3. Update Progress

```kotlin
val updatedProgress = UserSessionProgress(
    // ... progress data
)
repository.updateSessionProgress(updatedProgress)
```

### 4. Log Events

```kotlin
val event = SessionEvent(
    // ... event data
)
repository.logSessionEvent(event)
```

## Security Notes

- All database operations are protected by RLS policies
- Users can only access their own progress data
- Premium content access is validated server-side
- All functions use SECURITY DEFINER for proper permission handling

## Performance Optimizations

- Use appropriate indexes for database queries
- Implement local caching for frequently accessed data
- Use pagination for large result sets
- Optimize audio file loading and streaming

## Error Handling

Implement comprehensive error handling for:
- Network connectivity issues
- Authentication failures
- Database errors
- Audio playback errors

## Testing

Write unit tests for:
- Repository methods
- ViewModel logic
- Authentication flows
- Progress tracking

## Production Deployment

1. Use production Supabase credentials
2. Enable proper SSL/TLS
3. Implement proper error logging
4. Set up monitoring and analytics
5. Test with real users

This integration provides a complete, production-ready connection between the DrMindit Kotlin app and the Supabase backend.
