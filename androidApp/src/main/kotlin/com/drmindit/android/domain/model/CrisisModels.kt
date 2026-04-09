package com.drmindit.android.domain.model

/**
 * Represents different levels of crisis severity
 */
enum class CrisisLevel(val value: Int) {
    NONE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    IMMEDIATE(4)
}

/**
 * Represents a crisis detection alert
 */
data class CrisisAlert(
    val level: CrisisLevel,
    val detectedText: String,
    val timestamp: Long,
    val requiresImmediateAction: Boolean = false,
    val detectedKeywords: List<String> = emptyList(),
    val riskFactors: List<String>? = null,
    val recommendations: List<String> = emptyList()
)

/**
 * Crisis response action
 */
data class CrisisResponse(
    val action: CrisisAction,
    val message: String,
    val resources: List<CrisisResource>,
    val escalationRequired: Boolean
)

/**
 * Types of crisis actions
 */
enum class CrisisAction {
    PROVIDE_RESOURCES,
    SUGGEST_BREAK,
    CONTACT_SUPPORT,
    EMERGENCY_ESCALATION
}

/**
 * Crisis resource information
 */
data class CrisisResource(
    val name: String,
    val phoneNumber: String? = null,
    val website: String? = null,
    val description: String,
    val category: ResourceCategory
)

/**
 * Categories of crisis resources
 */
enum class ResourceCategory {
    HOTLINE,
    THERAPY,
    EMERGENCY,
    SUPPORT_GROUP,
    SELF_HELP
}
