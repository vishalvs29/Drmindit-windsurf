package com.drmindit.android.content

import com.drmindit.shared.domain.model.AudioSession
import com.drmindit.shared.domain.model.SessionCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Trauma-Informed Content Manager for Police/Military Personnel
 * Provides specialized content for high-stress professions
 */
@Singleton
class TraumaInformedContentManager @Inject constructor() {
    
    private val _userCategory = MutableStateFlow<UserCategory?>(null)
    val userCategory: Flow<UserCategory?> = _userCategory.asStateFlow()
    
    /**
     * Set user category for personalized content
     */
    fun setUserCategory(category: UserCategory) {
        _userCategory.value = category
    }
    
    /**
     * Get trauma-informed sessions for user category
     */
    fun getTraumaInformedSessions(category: UserCategory): List<TraumaInformedSession> {
        return when (category) {
            UserCategory.POLICE -> getPoliceSessions()
            UserCategory.MILITARY -> getMilitarySessions()
            UserCategory.FIRST_RESPONDER -> getFirstResponderSessions()
            UserCategory.GENERAL -> emptyList()
        }
    }
    
    /**
     * Get crisis protocols for specific professions
     */
    fun getCrisisProtocols(category: UserCategory): List<CrisisProtocol> {
        return when (category) {
            UserCategory.POLICE -> getPoliceCrisisProtocols()
            UserCategory.MILITARY -> getMilitaryCrisisProtocols()
            UserCategory.FIRST_RESPONDER -> getFirstResponderCrisisProtocols()
            UserCategory.GENERAL -> emptyList()
        }
    }
    
    /**
     * Get stress management techniques for profession
     */
    fun getStressManagementTechniques(category: UserCategory): List<StressManagementTechnique> {
        return when (category) {
            UserCategory.POLICE -> getPoliceStressTechniques()
            UserCategory.MILITARY -> getMilitaryStressTechniques()
            UserCategory.FIRST_RESPONDER -> getFirstResponderStressTechniques()
            UserCategory.GENERAL -> emptyList()
        }
    }
    
    /**
     * Police-specific sessions
     */
    private fun getPoliceSessions(): List<TraumaInformedSession> {
        return listOf(
            TraumaInformedSession(
                id = "police_debrief_01",
                title = "Critical Incident Debrief",
                description = "Structured debriefing after critical incidents to process trauma and prevent PTSD",
                duration = 1800, // 30 minutes
                category = SessionCategory.TRAUMA_SUPPORT,
                targetAudience = UserCategory.POLICE,
                contentLevel = ContentLevel.INTERMEDIATE,
                triggers = listOf("gunfire", "car accidents", "violence"),
                copingStrategies = listOf("grounding", "breathing", "cognitive reframing"),
                instructorName = "Dr. Sarah Chen, Police Psychology Specialist",
                tags = listOf("debrief", "critical incident", "trauma processing"),
                warningLabel = "Contains content related to police work incidents"
            ),
            TraumaInformedSession(
                id = "police_sleep_01",
                title = "Shift Work Sleep Optimization",
                description = "Techniques for improving sleep quality during irregular shift schedules",
                duration = 1200, // 20 minutes
                category = SessionCategory.SLEEP,
                targetAudience = UserCategory.POLICE,
                contentLevel = ContentLevel.BEGINNER,
                triggers = emptyList(),
                copingStrategies = listOf("sleep hygiene", "relaxation", "circadian rhythm"),
                instructorName = "Dr. Michael Torres, Sleep Specialist",
                tags = listOf("sleep", "shift work", "recovery"),
                warningLabel = null
            ),
            TraumaInformedSession(
                id = "police_vicarious_01",
                title = "Managing Vicarious Trauma",
                description = "Coping with secondary trauma from exposure to others' suffering",
                duration = 1500, // 25 minutes
                category = SessionCategory.STRESS_RELIEF,
                targetAudience = UserCategory.POLICE,
                contentLevel = ContentLevel.ADVANCED,
                triggers = listOf("child abuse", "domestic violence", "death"),
                copingStrategies = listOf("emotional boundaries", "self-care", "peer support"),
                instructorName = "Dr. Lisa Park, Trauma Specialist",
                tags = listOf("vicarious trauma", "compassion fatigue", "boundaries"),
                warningLabel = "Discusses exposure to traumatic events"
            ),
            TraumaInformedSession(
                id = "police_family_01",
                title = "Family Support After Critical Incidents",
                description = "Helping families understand and cope with police work stress",
                duration = 900, // 15 minutes
                category = SessionCategory.MINDFULNESS,
                targetAudience = UserCategory.POLICE,
                contentLevel = ContentLevel.BEGINNER,
                triggers = emptyList(),
                copingStrategies = listOf("family communication", "education", "support systems"),
                instructorName = "Dr. James Wilson, Family Therapist",
                tags = listOf("family", "relationships", "communication"),
                warningLabel = null
            )
        )
    }
    
    /**
     * Military-specific sessions
     */
    private fun getMilitarySessions(): List<TraumaInformedSession> {
        return listOf(
            TraumaInformedSession(
                id = "military_combat_01",
                title = "Combat Stress Management",
                description = "Evidence-based techniques for managing combat-related stress and anxiety",
                duration = 2400, // 40 minutes
                category = SessionCategory.TRAUMA_SUPPORT,
                targetAudience = UserCategory.MILITARY,
                contentLevel = ContentLevel.INTERMEDIATE,
                triggers = listOf("combat", "explosions", "injury"),
                copingStrategies = listOf("exposure therapy", "mindfulness", "peer support"),
                instructorName = "Dr. Robert Martinez, Military Psychologist",
                tags = listOf("combat stress", "PTSD", "military trauma"),
                warningLabel = "Contains combat-related content"
            ),
            TraumaInformedSession(
                id = "military_transition_01",
                title = "Civilian Reintegration Support",
                description = "Navigating the challenges of transitioning from military to civilian life",
                duration = 1800, // 30 minutes
                category = SessionCategory.MINDFULNESS,
                targetAudience = UserCategory.MILITARY,
                contentLevel = ContentLevel.BEGINNER,
                triggers = listOf("identity loss", "purpose", "belonging"),
                copingStrategies = listOf("identity work", "community building", "goal setting"),
                instructorName = "Dr. Amanda Foster, Transition Specialist",
                tags = listOf("transition", "reintegration", "identity"),
                warningLabel = null
            ),
            TraumaInformedSession(
                id = "military_moral_01",
                title = "Moral Injury Processing",
                description = "Addressing moral and ethical conflicts from military service",
                duration = 2000, // 33 minutes
                category = SessionCategory.TRAUMA_SUPPORT,
                targetAudience = UserCategory.MILITARY,
                contentLevel = ContentLevel.ADVANCED,
                triggers = listOf("moral conflict", "guilt", "shame"),
                copingStrategies = listOf("self-compassion", "values clarification", "forgiveness"),
                instructorName = "Dr. David Kim, Moral Injury Specialist",
                tags = listOf("moral injury", "ethics", "values"),
                warningLabel = "Discusses sensitive moral and ethical topics"
            ),
            TraumaInformedSession(
                id = "military_family_01",
                title = "Military Family Resilience",
                description = "Building family resilience during deployment and reintegration",
                duration = 1200, // 20 minutes
                category = SessionCategory.MINDFULNESS,
                targetAudience = UserCategory.MILITARY,
                contentLevel = ContentLevel.BEGINNER,
                triggers = listOf("deployment", "separation", "uncertainty"),
                copingStrategies = listOf("family routines", "communication", "support networks"),
                instructorName = "Dr. Jennifer Lee, Family Therapist",
                tags = listOf("family", "deployment", "resilience"),
                warningLabel = null
            )
        )
    }
    
    /**
     * First responder sessions
     */
    private fun getFirstResponderSessions(): List<TraumaInformedSession> {
        return listOf(
            TraumaInformedSession(
                id = "first_responder_ems_01",
                title = "EMS Trauma Resilience",
                description = "Building resilience for emergency medical services personnel",
                duration = 1500, // 25 minutes
                category = SessionCategory.TRAUMA_SUPPORT,
                targetAudience = UserCategory.FIRST_RESPONDER,
                contentLevel = ContentLevel.INTERMEDIATE,
                triggers = listOf("medical emergencies", "death", "injury"),
                copingStrategies = listOf("critical incident stress", "peer support", "self-care"),
                instructorName = "Dr. Rachel Green, EMS Psychology Specialist",
                tags = listOf("EMS", "trauma", "resilience"),
                warningLabel = "Contains medical emergency content"
            ),
            TraumaInformedSession(
                id = "first_responder_fire_01",
                title = "Firefighter Mental Wellness",
                description = "Mental health support for firefighters and fire service personnel",
                duration = 1800, // 30 minutes
                category = SessionCategory.STRESS_RELIEF,
                targetAudience = UserCategory.FIRST_RESPONDER,
                contentLevel = ContentLevel.INTERMEDIATE,
                triggers = listOf("fires", "rescues", "structural collapse"),
                copingStrategies = listOf("stress management", "team support", "recovery"),
                instructorName = "Dr. Thomas Brown, Fire Service Psychologist",
                tags = listOf("firefighter", "stress", "wellness"),
                warningLabel = "Contains fire-related content"
            )
        )
    }
    
    /**
     * Get crisis protocols for police
     */
    private fun getPoliceCrisisProtocols(): List<CrisisProtocol> {
        return listOf(
            CrisisProtocol(
                id = "police_critical_incident",
                title = "Critical Incident Response Protocol",
                description = "Immediate steps after officer-involved shootings or other critical incidents",
                steps = listOf(
                    "Ensure scene safety and secure weapons",
                    "Request peer support and chaplain",
                    "Contact critical incident stress team",
                    "Document timeline and observations",
                    "Schedule mandatory debrief within 24 hours"
                ),
                emergencyContacts = listOf("Police Peer Support: 1-800-COP-HELP", "Critical Incident Team: 1-800-STRESS-1"),
                timeSensitive = true
            ),
            CrisisProtocol(
                id = "police_suicide_prevention",
                title = "Officer Suicide Prevention Protocol",
                description = "Recognizing and responding to suicide risk among law enforcement personnel",
                steps = listOf(
                    "Immediate safety assessment if risk identified",
                    "Remove access to firearms",
                    "Contact mental health professional",
                    "Inform trusted family member or supervisor",
                    "Follow up monitoring schedule"
                ),
                emergencyContacts = listOf("Suicide Prevention Hotline: 988", "Officer Support: 1-800-COP-SAFE"),
                timeSensitive = true
            )
        )
    }
    
    /**
     * Get crisis protocols for military
     */
    private fun getMilitaryCrisisProtocols(): List<CrisisProtocol> {
        return listOf(
            CrisisProtocol(
                id = "military_combat_stress",
                title = "Combat Stress Reaction Protocol",
                description = "Immediate response to acute combat stress reactions",
                steps = listOf(
                    "Remove from immediate threat environment",
                    "Provide basic needs (food, water, rest)",
                    "Encourage verbal processing of experience",
                    "Reassure about normal stress reactions",
                    "Connect with unit mental health resources"
                ),
                emergencyContacts = listOf("Military Crisis Line: 988-Press1", "VA Crisis: 1-800-273-8255"),
                timeSensitive = true
            ),
            CrisisProtocol(
                id = "military_suicide_prevention",
                title = "Service Member Suicide Prevention Protocol",
                description = "Emergency response to suicide risk in military settings",
                steps = listOf(
                    "Immediate safety intervention",
                    "Remove access to weapons",
                    "Notify chain of command",
                    "Contact mental health emergency services",
                    "Arrange continuous monitoring"
                ),
                emergencyContacts = listOf("Military Crisis: 988-Press1", "Veterans Crisis: 1-800-273-8255"),
                timeSensitive = true
            )
        )
    }
    
    /**
     * Get crisis protocols for first responders
     */
    private fun getFirstResponderCrisisProtocols(): List<CrisisProtocol> {
        return listOf(
            CrisisProtocol(
                id = "first_responder_critical",
                title = "First Responder Critical Incident Protocol",
                description = "Response protocols for first responders after traumatic events",
                steps = listOf(
                    "Scene safety and accountability",
                    "Peer support activation",
                    "Family notification procedures",
                    "Critical incident stress debrief scheduling",
                    "Follow-up monitoring and support"
                ),
                emergencyContacts = listOf("FirstResponder Support: 1-800-HELP-911", "Crisis Line: 988"),
                timeSensitive = true
            )
        )
    }
    
    /**
     * Get stress management techniques
     */
    private fun getPoliceStressTechniques(): List<StressManagementTechnique> {
        return listOf(
            StressManagementTechnique(
                id = "police Tactical Breathing",
                title = "Tactical Breathing for Law Enforcement",
                description = "Quick breathing techniques for high-stress police situations",
                duration = 300, // 5 minutes
                instructions = listOf(
                    "Inhale for 4 counts through nose",
                    "Hold for 4 counts",
                    "Exhale for 4 counts through mouth",
                    "Hold for 4 counts",
                    "Repeat 4-8 cycles"
                ),
                effectiveness = Effectiveness.HIGH,
                difficulty = Difficulty.BEGINNER
            ),
            StressManagementTechnique(
                id = "police Grounding",
                title = "5-4-3-2-1 Grounding Technique",
                description = "Sensory grounding technique for acute stress reactions",
                duration = 180, // 3 minutes
                instructions = listOf(
                    "Name 5 things you can see",
                    "Name 4 things you can touch",
                    "Name 3 things you can hear",
                    "Name 2 things you can smell",
                    "Name 1 thing you can taste"
                ),
                effectiveness = Effectiveness.HIGH,
                difficulty = Difficulty.BEGINNER
            )
        )
    }
    
    private fun getMilitaryStressTechniques(): List<StressManagementTechnique> {
        return listOf(
            StressManagementTechnique(
                id = "military Box Breathing",
                title = "Box Breathing for Combat Stress",
                description = "Military-validated breathing technique for stress control",
                duration = 240, // 4 minutes
                instructions = listOf(
                    "Inhale for 4 counts",
                    "Hold for 4 counts",
                    "Exhale for 4 counts",
                    "Hold for 4 counts",
                    "Repeat until calm"
                ),
                effectiveness = Effectiveness.HIGH,
                difficulty = Difficulty.BEGINNER
            )
        )
    }
    
    private fun getFirstResponderStressTechniques(): List<StressManagementTechnique> {
        return listOf(
            StressManagementTechnique(
                id = "first_responder Progressive Muscle",
                title = "Progressive Muscle Relaxation",
                description = "Rapid tension-release technique for first responders",
                duration = 600, // 10 minutes
                instructions = listOf(
                    "Tense and relax major muscle groups",
                    "Start with feet and work upward",
                    "Hold tension for 5 seconds",
                    "Release for 10 seconds",
                    "Focus on relaxation sensation"
                ),
                effectiveness = Effectiveness.MEDIUM,
                difficulty = Difficulty.INTERMEDIATE
            )
        )
    }
}

/**
 * Data classes for trauma-informed content
 */
data class TraumaInformedSession(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int, // seconds
    val category: SessionCategory,
    val targetAudience: UserCategory,
    val contentLevel: ContentLevel,
    val triggers: List<String>,
    val copingStrategies: List<String>,
    val instructorName: String,
    val tags: List<String>,
    val warningLabel: String?
)

data class CrisisProtocol(
    val id: String,
    val title: String,
    val description: String,
    val steps: List<String>,
    val emergencyContacts: List<String>,
    val timeSensitive: Boolean
)

data class StressManagementTechnique(
    val id: String,
    val title: String,
    val description: String,
    val duration: Int, // seconds
    val instructions: List<String>,
    val effectiveness: Effectiveness,
    val difficulty: Difficulty
)

/**
 * Enums for content categorization
 */
enum class UserCategory {
    GENERAL, POLICE, MILITARY, FIRST_RESPONDER
}

enum class ContentLevel {
    BEGINNER, INTERMEDIATE, ADVANCED
}

enum class Effectiveness {
    LOW, MEDIUM, HIGH
}

enum class Difficulty {
    BEGINNER, INTERMEDIATE, ADVANCED
}
