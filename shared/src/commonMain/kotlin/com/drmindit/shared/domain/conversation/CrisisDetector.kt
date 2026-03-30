package com.drmindit.shared.domain.conversation

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Crisis Detection System
 * Identifies crisis situations and triggers immediate intervention
 */
@Singleton
class CrisisDetector @Inject constructor() {
    
    /**
     * Detect crisis severity from user message
     */
    fun detectCrisis(message: String, userProfile: UserProfile): Severity {
        val normalizedMessage = message.lowercase().trim()
        
        // Check for immediate danger indicators
        if (containsImmediateDanger(normalizedMessage)) {
            return Severity.CRITICAL
        }
        
        // Check for self-harm indicators
        if (containsSelfHarm(normalizedMessage)) {
            return Severity.CRITICAL
        }
        
        // Check for suicide indicators
        if (containsSuicidalIdeation(normalizedMessage)) {
            return Severity.CRITICAL
        }
        
        // Check for severe mental health crisis
        if (containsSevereCrisis(normalizedMessage)) {
            return Severity.HIGH
        }
        
        // Check for moderate crisis indicators
        if (containsModerateCrisis(normalizedMessage)) {
            return Severity.MODERATE
        }
        
        return Severity.LOW
    }
    
    /**
     * Generate crisis assessment
     */
    fun generateCrisisAssessment(message: String, userProfile: UserProfile): CrisisAssessment {
        val normalizedMessage = message.lowercase().trim()
        val severity = detectCrisis(message, userProfile)
        
        val riskFactors = identifyRiskFactors(normalizedMessage, userProfile)
        val protectiveFactors = identifyProtectiveFactors(normalizedMessage, userProfile)
        val immediateActions = determineImmediateActions(severity, riskFactors)
        val resources = getEmergencyResources(severity)
        
        return CrisisAssessment(
            severity = severity,
            riskFactors = riskFactors,
            protectiveFactors = protectiveFactors,
            immediateActions = immediateActions,
            resources = resources,
            requiresProfessionalHelp = severity >= Severity.MODERATE,
            requiresEmergencyServices = severity >= Severity.CRITICAL,
            assessmentTimestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Check if message contains immediate danger indicators
     */
    private fun containsImmediateDanger(message: String): Boolean {
        val immediateDangerKeywords = listOf(
            "going to kill myself", "going to end my life", "going to hurt myself",
            "about to kill myself", "about to end my life", "about to hurt myself",
            "planning to kill myself", "planning to end my life", "planning to hurt myself",
            "have a plan", "have the means", "ready to end it", "ready to die",
            "tonight", "right now", "immediately", "going to do it"
        )
        
        return immediateDangerKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    /**
     * Check if message contains self-harm indicators
     */
    private fun containsSelfHarm(message: String): Boolean {
        val selfHarmKeywords = listOf(
            "cut myself", "cutting myself", "self harm", "self-harm",
            "hurt myself", "injure myself", "burn myself", "overdose",
            "taking pills", "too many pills", "poison", "hanging",
            "jumping", "drowning", "shooting myself", "stabbing"
        )
        
        return selfHarmKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    /**
     * Check if message contains suicidal ideation
     */
    private fun containsSuicidalIdeation(message: String): Boolean {
        val suicidalKeywords = listOf(
            "suicide", "suicidal", "kill myself", "end my life", "want to die",
            "want to kill myself", "want to end my life", "don't want to live",
            "better off dead", "no reason to live", "life isn't worth living",
            "can't go on", "can't take it anymore", "giving up", "ending it all",
            "no way out", "no escape", "final solution", "permanent solution"
        )
        
        return suicidalKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    /**
     * Check if message contains severe crisis indicators
     */
    private fun containsSevereCrisis(message: String): Boolean {
        val severeCrisisKeywords = listOf(
            "completely overwhelmed", "can't function", "can't cope",
            "losing my mind", "going crazy", "mental breakdown", "breakdown",
            "can't get out of bed", "can't eat", "can't sleep", "can't work",
            "everything is hopeless", "nothing matters", "no point",
            "completely lost", "totally lost", "no hope", "desperate"
        )
        
        return severeCrisisKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    /**
     * Check if message contains moderate crisis indicators
     */
    private fun containsModerateCrisis(message: String): Boolean {
        val moderateCrisisKeywords = listOf(
            "overwhelmed", "struggling", "having a hard time", "difficult time",
            "need help", "need support", "can't handle this", "too much",
            "stressed out", "burned out", "exhausted", "drained",
            "feeling lost", "confused", "worried", "anxious", "depressed"
        )
        
        return moderateCrisisKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
    
    /**
     * Identify risk factors from message and user profile
     */
    private fun identifyRiskFactors(message: String, userProfile: UserProfile): List<String> {
        val riskFactors = mutableListOf<String>()
        
        // Check for known risk factors in user profile
        riskFactors.addAll(userProfile.riskFactors)
        
        // Check for risk factors in message
        if (message.contains("alone") || message.contains("lonely")) {
            riskFactors.add("social_isolation")
        }
        
        if (message.contains("substance") || message.contains("alcohol") || message.contains("drugs")) {
            riskFactors.add("substance_use")
        }
        
        if (message.contains("recent_loss") || message.contains("death") || message.contains("grief")) {
            riskFactors.add("recent_loss")
        }
        
        if (message.contains("relationship") || message.contains("breakup") || message.contains("divorce")) {
            riskFactors.add("relationship_problems")
        }
        
        if (message.contains("financial") || message.contains("money") || message.contains("debt")) {
            riskFactors.add("financial_problems")
        }
        
        if (message.contains("work") || message.contains("job") || message.contains("unemployment")) {
            riskFactors.add("work_problems")
        }
        
        if (message.contains("health") || message.contains("illness") || message.contains("pain")) {
            riskFactors.add("health_problems")
        }
        
        return riskFactors.distinct()
    }
    
    /**
     * Identify protective factors from message and user profile
     */
    private fun identifyProtectiveFactors(message: String, userProfile: UserProfile): List<String> {
        val protectiveFactors = mutableListOf<String>()
        
        // Check for known protective factors in user profile
        protectiveFactors.addAll(userProfile.strengths)
        
        // Check for protective factors in message
        if (message.contains("family") || message.contains("friends") || message.contains("support")) {
            protectiveFactors.add("social_support")
        }
        
        if (message.contains("therapy") || message.contains("therapist") || message.contains("counseling")) {
            protectiveFactors.add("professional_help")
        }
        
        if (message.contains("medication") || message.contains("medicine") || message.contains("treatment")) {
            protectiveFactors.add("medical_treatment")
        }
        
        if (message.contains("hope") || message.contains("optimistic") || message.contains("better")) {
            protectiveFactors.add("hope")
        }
        
        if (message.contains("faith") || message.contains("spiritual") || message.contains("religion")) {
            protectiveFactors.add("spiritual_support")
        }
        
        if (message.contains("children") || message.contains("kids") || message.contains("family")) {
            protectiveFactors.add("family_responsibility")
        }
        
        if (message.contains("pets") || message.contains("animal") || message.contains("dog") || message.contains("cat")) {
            protectiveFactors.add("pet_support")
        }
        
        return protectiveFactors.distinct()
    }
    
    /**
     * Determine immediate actions based on severity and risk factors
     */
    private fun determineImmediateActions(severity: Severity, riskFactors: List<String>): List<String> {
        val actions = mutableListOf<String>()
        
        when (severity) {
            Severity.CRITICAL -> {
                actions.addAll(listOf(
                    "call_emergency_services",
                    "contact_crisis_hotline",
                    "ensure_immediate_safety",
                    "remove_means_of_harm",
                    "stay_with_person"
                ))
            }
            Severity.HIGH -> {
                actions.addAll(listOf(
                    "contact_crisis_hotline",
                    "reach_out_to_trusted_contact",
                    "remove_immediate_dangers",
                    "provide_constant_support",
                    "consider_emergency_services"
                ))
            }
            Severity.MODERATE -> {
                actions.addAll(listOf(
                    "contact_professional_help",
                    "reach_out_to_support_system",
                    "provide_coping_strategies",
                    "schedule_therapy_appointment",
                    "create_safety_plan"
                ))
            }
            Severity.LOW -> {
                actions.addAll(listOf(
                    "provide_emotional_support",
                    "suggest_coping_techniques",
                    "recommend_resources",
                    "encourage_self_care",
                    "follow_up_soon"
                ))
            }
        }
        
        // Add specific actions based on risk factors
        if (riskFactors.contains("substance_use")) {
            actions.add("contact_substance_abuse_hotline")
        }
        
        if (riskFactors.contains("social_isolation")) {
            actions.add("connect_with_support_network")
        }
        
        return actions.distinct()
    }
    
    /**
     * Get emergency resources based on severity
     */
    private fun getEmergencyResources(severity: Severity): List<EmergencyResource> {
        return when (severity) {
            Severity.CRITICAL -> listOf(
                EmergencyResource(
                    id = "emergency_112",
                    name = "Emergency Services",
                    description = "Call 112 for India emergency services",
                    contact = "112",
                    type = ResourceType.HOTLINE,
                    priority = 1,
                    is24Hours = true
                ),
                EmergencyResource(
                    id = "crisis_text_line",
                    name = "Crisis Text Line",
                    description = "Text iCall for 24/7 crisis support",
                    contact = "iCall 9152987821",
                    type = ResourceType.HOTLINE,
                    priority = 2,
                    is24Hours = true
                ),
                EmergencyResource(
                    id = "suicide_hotline",
                    name = "Suicide Prevention Hotline",
                    description = "Call 9999666555 for suicide prevention support",
                    contact = "9999666555",
                    type = ResourceType.HOTLINE,
                    priority = 2,
                    is24Hours = true
                )
            )
            Severity.HIGH -> listOf(
                EmergencyResource(
                    id = "crisis_hotline_9999666555",
                    name = "Crisis Hotline",
                    description = "Call 9999666555 for 24/7 crisis support",
                    contact = "9999666555",
                    type = ResourceType.HOTLINE,
                    priority = 1,
                    is24Hours = true
                ),
                EmergencyResource(
                    id = "emergency_services",
                    name = "Emergency Services",
                    description = "Call 112 if you're in immediate danger",
                    contact = "112",
                    type = ResourceType.HOTLINE,
                    priority = 2,
                    is24Hours = true
                ),
                EmergencyResource(
                    id = "local_emergency",
                    name = "Local Emergency Services",
                    description = "Contact local emergency services or go to nearest hospital",
                    contact = "Local emergency number",
                    type = ResourceType.HOTLINE,
                    priority = 3,
                    is24Hours = true
                )
            )
            Severity.MODERATE -> listOf(
                EmergencyResource(
                    id = "therapy_referral",
                    name = "Therapy Referral",
                    description = "Connect with mental health professionals",
                    contact = "Find local therapist",
                    type = ResourceType.PROFESSIONAL,
                    priority = 1,
                    is24Hours = false
                ),
                EmergencyResource(
                    id = "support_groups",
                    name = "Support Groups",
                    description = "Join peer support groups for shared experiences",
                    contact = "Find local support group",
                    type = ResourceType.COMMUNITY,
                    priority = 2,
                    is24Hours = false
                ),
                EmergencyResource(
                    id = "crisis_resources",
                    name = "Crisis Resources",
                    description = "Comprehensive crisis support resources",
                    contact = "Available resources",
                    type = ResourceType.RESOURCE_LINK,
                    priority = 3,
                    is24Hours = false
                )
            )
            Severity.LOW -> listOf(
                EmergencyResource(
                    id = "self_help_resources",
                    name = "Self-Help Resources",
                    description = "Tools and techniques for self-support",
                    contact = "Available resources",
                    type = ResourceType.RESOURCE_LINK,
                    priority = 1,
                    is24Hours = false
                ),
                EmergencyResource(
                    id = "wellness_apps",
                    name = "Mental Wellness Apps",
                    description = "Apps for mental health support and tracking",
                    contact = "App stores",
                    type = ResourceType.APP,
                    priority = 2,
                    is24Hours = false
                )
            )
        }
    }
    
    /**
     * Generate crisis response message
     */
    fun generateCrisisResponse(message: String, userProfile: UserProfile): CrisisResponse {
        val severity = detectCrisis(message, userProfile)
        val assessment = generateCrisisAssessment(message, userProfile)
        
        val responseContent = when (severity) {
            Severity.CRITICAL -> generateCriticalResponse(assessment)
            Severity.HIGH -> generateHighSeverityResponse(assessment)
            Severity.MODERATE -> generateModerateSeverityResponse(assessment)
            Severity.LOW -> generateLowSeverityResponse(assessment)
        }
        
        return CrisisResponse(
            content = responseContent,
            severity = severity,
            assessment = assessment,
            requiresImmediateAction = severity >= Severity.HIGH,
            followUpRequired = true,
            responseTimestamp = System.currentTimeMillis()
        )
    }
    
    /**
     * Generate critical severity response
     */
    private fun generateCriticalResponse(assessment: CrisisAssessment): String {
        return """
            I'm deeply concerned about your safety right now. Your life is valuable, and I want you to be safe.
            
            IMMEDIATE ACTIONS:
            🚨 **Call 112** or go to nearest emergency room
            📱 **Text iCall to 9152987821** for 24/7 crisis support
            📞 **Call 9999666555** for suicide prevention support
            
            RIGHT NOW:
            • Remove any means of harm from your area
            • Stay with someone if possible
            • Don't be alone - reach out to family, friends, or emergency services
            
            You are not alone in this. Help is available 24/7, and people care about you. Please reach out to one of the resources above immediately.
            
            I'll stay here with you while you get help. You matter, and your safety is the most important thing right now.
        """.trimIndent()
    }
    
    /**
     * Generate high severity response
     */
    private fun generateHighSeverityResponse(assessment: CrisisAssessment): String {
        return """
            I can hear that you're going through a really difficult time, and I want you to know that help is available.
            
            IMMEDIATE SUPPORT:
            📞 **Call 9999666555** for 24/7 crisis support
            📱 **Text iCall to 9152987821** for crisis text support
            🏥 **Contact local emergency services** if you're in danger
            
            WHILE YOU WAIT FOR HELP:
            🧘‍⚕️ **Try deep breathing**: Inhale for 4 counts, hold for 4, exhale for 4
            📍 **Ground yourself**: Name 5 things you can see, 4 things you can touch
            🤝 **Reach out**: Contact a trusted friend, family member, or crisis line
            
            You don't have to go through this alone. There are people who want to help you right now. Please reach out to one of the resources above.
            
            Your wellbeing matters deeply. I'm here to support you while you get help you need.
        """.trimIndent()
    }
    
    /**
     * Generate moderate severity response
     */
    private fun generateModerateSeverityResponse(assessment: CrisisAssessment): String {
        return """
            I can hear that you're struggling right now, and I want you to know that your feelings are valid. You're not alone in this experience.
            
            SUPPORT OPTIONS:
            🧑‍⚕️ **Professional Help**: Consider reaching out to a therapist or counselor
            📞 **Crisis Support**: Call 988 if you need immediate support
            🤝 **Support Network**: Reach out to trusted friends or family
            📚 **Resources**: Use mental health apps and online resources
            
            COPING STRATEGIES:
            🌱 **Self-compassion**: Treat yourself with kindness
            🧘 **Mindfulness**: Practice present-moment awareness
            🏃 **Movement**: Gentle exercise can help
            📝 **Journaling**: Write down your thoughts and feelings
            
            You're taking an important step by reaching out. Consider scheduling an appointment with a mental health professional this week.
            
            I'm here to support you through this. You deserve help and healing.
        """.trimIndent()
    }
    
    /**
     * Generate low severity response
     */
    private fun generateLowSeverityResponse(assessment: CrisisAssessment): String {
        return """
            I hear that you're having a difficult time, and I'm here to support you. It's brave to reach out when you're struggling.
            
            SUPPORTIVE ACTIONS:
            🤝 **Connect**: Reach out to someone you trust
            🧘 **Breathe**: Try deep breathing exercises
            🌱 **Self-care**: Take care of your basic needs
            📱 **Resources**: Use mental wellness apps and resources
            
            REMEMBER:
            • Your feelings are valid and temporary
            • You've overcome difficult times before
            • Support is available when you need it
            • Small steps can lead to big changes
            
            I'm here to listen and support you. Would you like to talk more about what's been difficult lately?
            
            You don't have to carry this alone. I'm here with you.
        """.trimIndent()
    }
}

/**
 * Crisis assessment data
 */
data class CrisisAssessment(
    val severity: Severity,
    val riskFactors: List<String>,
    val protectiveFactors: List<String>,
    val immediateActions: List<String>,
    val resources: List<EmergencyResource>,
    val requiresProfessionalHelp: Boolean,
    val requiresEmergencyServices: Boolean,
    val assessmentTimestamp: Long
)

/**
 * Emergency resource data
 */
data class EmergencyResource(
    val id: String,
    val name: String,
    val description: String,
    val contact: String,
    val type: ResourceType,
    val priority: Int,
    val is24Hours: Boolean
)

/**
 * Crisis response data
 */
data class CrisisResponse(
    val content: String,
    val severity: Severity,
    val assessment: CrisisAssessment,
    val requiresImmediateAction: Boolean,
    val followUpRequired: Boolean,
    val responseTimestamp: Long
)
