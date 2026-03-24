package com.drmindit.shared.data.repository

import com.drmindit.shared.domain.model.Mood
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class CrisisEventRow(
    val id: String? = null,
    val user_id: String,
    val trigger_reason: String,
    val severity: String,
    val mood_score: Int? = null,
    val context_data: Map<String, Any> = emptyMap(),
    val is_resolved: Boolean = false,
    val resolution_notes: String? = null,
    val follow_up_required: Boolean = true,
    val organization_notified: Boolean = false,
    val created_at: String? = null,
    val resolved_at: String? = null
)

@Serializable
data class EmergencyHelplineRow(
    val id: String,
    val name: String,
    val phone: String,
    val email: String? = null,
    val website: String? = null,
    val description: String? = null,
    val country: String = "IN",
    val is_24_7: Boolean = true,
    val languages: List<String> = listOf("en"),
    val is_active: Boolean = true,
    val priority: Int = 0,
    val created_at: String? = null
)

@Serializable
data class MoodLogRow(
    val id: String? = null,
    val user_id: String,
    val mood_score: Int,
    val stress_level: Int? = null,
    val sleep_quality: String? = null,
    val mood_type: String? = null,
    val notes: String? = null,
    val ai_analysis: Map<String, Any>? = null,
    val created_at: String? = null
)

enum class CrisisTriggerReason {
    LOW_MOOD_SCORE,
    SUICIDAL_KEYWORDS,
    REPEATED_NEGATIVE_LOGS,
    AI_DETECTED_DISTRESS,
    MANUAL_TRIGGER
}

enum class CrisisSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

class CrisisRepository(
    private val supabase: SupabaseClient = SupabaseClient.client,
    private val database: Postgrest = supabase.database
) {
    
    // Crisis detection keywords
    private val suicidalKeywords = setOf(
        "suicidal", "want to die", "kill myself", "end my life",
        "suicide", "no reason to live", "better off dead",
        "hopeless", "worthless", "burden", "give up"
    )
    
    suspend fun detectCrisis(
        userId: String,
        moodScore: Int? = null,
        notes: String? = null,
        moodType: Mood? = null
    ): Result<CrisisDetectionResult> {
        return try {
            val triggers = mutableListOf<CrisisTriggerReason>()
            var severity = CrisisSeverity.LOW
            
            // Check for low mood score
            if (moodScore != null && moodScore <= 2) {
                triggers.add(CrisisTriggerReason.LOW_MOOD_SCORE)
                severity = when (moodScore) {
                    1 -> CrisisSeverity.CRITICAL
                    2 -> CrisisSeverity.HIGH
                    else -> CrisisSeverity.MEDIUM
                }
            }
            
            // Check for suicidal keywords in notes
            if (notes != null) {
                val lowerNotes = notes.lowercase()
                val foundKeywords = suicidalKeywords.filter { keyword ->
                    lowerNotes.contains(keyword)
                }
                
                if (foundKeywords.isNotEmpty()) {
                    triggers.add(CrisisTriggerReason.SUICIDAL_KEYWORDS)
                    severity = CrisisSeverity.CRITICAL
                }
            }
            
            // Check for negative mood type
            if (moodType != null) {
                when (moodType) {
                    Mood.VERY_SAD -> {
                        triggers.add(CrisisTriggerReason.AI_DETECTED_DISTRESS)
                        severity = CrisisSeverity.HIGH
                    }
                    Mood.SAD -> {
                        triggers.add(CrisisTriggerReason.AI_DETECTED_DISTRESS)
                        severity = CrisisSeverity.MEDIUM
                    }
                    Mood.ANXIOUS -> {
                        triggers.add(CrisisTriggerReason.AI_DETECTED_DISTRESS)
                        severity = CrisisSeverity.MEDIUM
                    }
                    else -> { /* No action needed */ }
                }
            }
            
            // Check for repeated negative logs (last 7 days)
            val recentNegativeLogs = getRecentNegativeLogs(userId)
            if (recentNegativeLogs >= 5) {
                triggers.add(CrisisTriggerReason.REPEATED_NEGATIVE_LOGS)
                severity = CrisisSeverity.HIGH
            }
            
            val isCrisis = triggers.isNotEmpty()
            
            if (isCrisis) {
                // Log crisis event
                val crisisEvent = CrisisEventRow(
                    user_id = userId,
                    trigger_reason = triggers.first().name.lowercase(),
                    severity = severity.name.lowercase(),
                    mood_score = moodScore,
                    context_data = mapOf(
                        "triggers" to triggers.map { it.name },
                        "notes" to (notes ?: ""),
                        "mood_type" to (moodType?.name ?: ""),
                        "detection_timestamp" to Clock.System.now().toString()
                    )
                )
                
                database.from("crisis_events").insert(crisisEvent)
                
                // Update user risk status
                updateUserRiskStatus(userId)
            }
            
            Result.success(
                CrisisDetectionResult(
                    isCrisis = isCrisis,
                    triggers = triggers,
                    severity = severity,
                    moodScore = moodScore,
                    notes = notes,
                    moodType = moodType
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logCrisisEvent(
        userId: String,
        triggerReason: CrisisTriggerReason,
        severity: CrisisSeverity,
        moodScore: Int? = null,
        contextData: Map<String, Any> = emptyMap()
    ): Result<String> {
        return try {
            val crisisEvent = CrisisEventRow(
                user_id = userId,
                trigger_reason = triggerReason.name.lowercase(),
                severity = severity.name.lowercase(),
                mood_score = moodScore,
                context_data = contextData
            )
            
            val result = database.from("crisis_events").insert(crisisEvent)
            Result.success("Crisis event logged successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getEmergencyHelplines(country: String = "IN"): Result<List<EmergencyHelpline>> {
        return try {
            val helplines = database.from("emergency_helplines")
                .select {
                    EmergencyHelplineRow::id
                    EmergencyHelplineRow::name
                    EmergencyHelplineRow::phone
                    EmergencyHelplineRow::email
                    EmergencyHelplineRow::website
                    EmergencyHelplineRow::description
                    EmergencyHelplineRow::country
                    EmergencyHelplineRow::is_24_7
                    EmergencyHelplineRow::languages
                    EmergencyHelplineRow::priority
                }
                .eq("is_active", true)
                .eq("country", country)
                .order("priority")
                .data
                ?.map { mapHelplineRowToHelpline(it) }
                ?: emptyList()
            
            Result.success(helplines)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserCrisisHistory(userId: String): Result<List<CrisisEvent>> {
        return try {
            val events = database.from("crisis_events")
                .select()
                .eq("user_id", userId)
                .order("created_at", ascending = false)
                .limit(50)
                .data
                ?.map { mapCrisisRowToEvent(it) }
                ?: emptyList()
            
            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resolveCrisisEvent(
        eventId: String,
        resolutionNotes: String? = null
    ): Result<Unit> {
        return try {
            database.from("crisis_events")
                .update(
                    mapOf(
                        "is_resolved" to true,
                        "resolution_notes" to resolutionNotes,
                        "resolved_at" to Clock.System.now().toString()
                    )
                )
                .eq("id", eventId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun getRecentNegativeLogs(userId: String): Int {
        return try {
            val sevenDaysAgo = Clock.System.now().minus(kotlin.datetime.DateTimePeriod(days = 7))
            
            val count = database.from("mood_logs")
                .select()
                .eq("user_id", userId)
                .gte("created_at", sevenDaysAgo.toString())
                .lt("mood_score", 4) // Negative mood scores
                .data
                ?.size ?: 0
            
            count
        } catch (e: Exception) {
            0
        }
    }
    
    private suspend fun updateUserRiskStatus(userId: String) {
        try {
            // Call the database function to update risk status
            database.rpc("update_user_risk_status", mapOf("user_uuid" to userId))
        } catch (e: Exception) {
            // Log error but don't fail the crisis detection
            println("Failed to update user risk status: ${e.message}")
        }
    }
    
    private fun mapHelplineRowToHelpline(row: EmergencyHelplineRow): EmergencyHelpline {
        return EmergencyHelpline(
            id = row.id,
            name = row.name,
            phone = row.phone,
            email = row.email,
            website = row.website,
            description = row.description,
            country = row.country,
            is24x7 = row.is_24_7,
            languages = row.languages,
            priority = row.priority
        )
    }
    
    private fun mapCrisisRowToEvent(row: CrisisEventRow): CrisisEvent {
        return CrisisEvent(
            id = row.id ?: "",
            userId = row.user_id,
            triggerReason = CrisisTriggerReason.valueOf(row.trigger_reason.uppercase()),
            severity = CrisisSeverity.valueOf(row.severity.uppercase()),
            moodScore = row.mood_score,
            contextData = row.context_data,
            isResolved = row.is_resolved,
            resolutionNotes = row.resolution_notes,
            followUpRequired = row.follow_up_required,
            organizationNotified = row.organization_notified,
            createdAt = row.created_at ?: "",
            resolvedAt = row.resolved_at
        )
    }
}

data class CrisisDetectionResult(
    val isCrisis: Boolean,
    val triggers: List<CrisisTriggerReason>,
    val severity: CrisisSeverity,
    val moodScore: Int?,
    val notes: String?,
    val moodType: Mood?
)

data class EmergencyHelpline(
    val id: String,
    val name: String,
    val phone: String,
    val email: String?,
    val website: String?,
    val description: String?,
    val country: String,
    val is24x7: Boolean,
    val languages: List<String>,
    val priority: Int
)

data class CrisisEvent(
    val id: String,
    val userId: String,
    val triggerReason: CrisisTriggerReason,
    val severity: CrisisSeverity,
    val moodScore: Int?,
    val contextData: Map<String, Any>,
    val isResolved: Boolean,
    val resolutionNotes: String?,
    val followUpRequired: Boolean,
    val organizationNotified: Boolean,
    val createdAt: String,
    val resolvedAt: String?
)
