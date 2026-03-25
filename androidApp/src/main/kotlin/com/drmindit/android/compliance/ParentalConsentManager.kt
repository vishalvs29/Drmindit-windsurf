package com.drmindit.android.compliance

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parental Consent Manager for users under 18
 * Handles age verification and parental consent workflows
 */
@Singleton
class ParentalConsentManager @Inject constructor(
    private val context: Context
) {
    
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val encryptedPrefs: EncryptedSharedPreferences
    
    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setKeySize(MasterKey.KEY_SIZE_256)
            .build()
        
        encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            "parental_consent_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    private val _consentStatus = MutableStateFlow<ParentalConsentStatus?>(null)
    val consentStatus: Flow<ParentalConsentStatus?> = _consentStatus.asStateFlow()
    
    /**
     * Check if user is under 18
     */
    fun isUserUnder18(): Boolean {
        return encryptedPrefs.getBoolean(KEY_USER_UNDER_18, false)
    }
    
    /**
     * Get user's birth date
     */
    fun getUserBirthDate(): Long? {
        val timestamp = encryptedPrefs.getLong(KEY_BIRTH_DATE, 0L)
        return if (timestamp > 0) timestamp else null
    }
    
    /**
     * Set user's age information
     */
    fun setUserAgeInfo(birthDate: Long, isUnder18: Boolean) {
        encryptedPrefs.edit()
            .putLong(KEY_BIRTH_DATE, birthDate)
            .putBoolean(KEY_USER_UNDER_18, isUnder18)
            .apply()
    }
    
    /**
     * Check if parental consent is required
     */
    fun isParentalConsentRequired(): Boolean {
        return isUserUnder18() && !hasParentalConsent()
    }
    
    /**
     * Check if parental consent has been granted
     */
    fun hasParentalConsent(): Boolean {
        return encryptedPrefs.getBoolean(KEY_PARENTAL_CONSENT, false)
    }
    
    /**
     * Get parental consent status
     */
    suspend fun getParentalConsentStatus(userId: String): Result<ParentalConsentStatus> {
        return try {
            val consentId = encryptedPrefs.getString(KEY_PARENTAL_CONSENT_ID, null)
            
            if (consentId != null) {
                val document = firestore.collection("parental_consents")
                    .document(consentId)
                    .get()
                    .await()
                
                if (document.exists()) {
                    val status = ParentalConsentStatus(
                        userId = userId,
                        isUnder18 = true,
                        consentRequired = true,
                        consentGranted = document.getBoolean("granted") ?: false,
                        consentId = consentId,
                        parentEmail = document.getString("parentEmail"),
                        consentTimestamp = document.getLong("timestamp"),
                        expiryDate = document.getLong("expiryDate"),
                        status = ParentalConsentState.valueOf(
                            document.getString("status") ?: ParentalConsentState.PENDING.name
                        )
                    )
                    
                    _consentStatus.value = status
                    Result.success(status)
                } else {
                    Result.success(
                        ParentalConsentStatus(
                            userId = userId,
                            isUnder18 = true,
                            consentRequired = true,
                            consentGranted = false,
                            consentId = null,
                            status = ParentalConsentState.NOT_REQUESTED
                        )
                    )
                }
            } else {
                Result.success(
                    ParentalConsentStatus(
                        userId = userId,
                        isUnder18 = true,
                        consentRequired = true,
                        consentGranted = false,
                        consentId = null,
                        status = ParentalConsentState.NOT_REQUESTED
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Request parental consent
     */
    suspend fun requestParentalConsent(
        userId: String,
        userEmail: String,
        parentEmail: String,
        childName: String
    ): Result<ParentalConsentRequest> {
        return try {
            val requestId = "pcr_${System.currentTimeMillis()}"
            val request = ParentalConsentRequest(
                id = requestId,
                userId = userId,
                userEmail = userEmail,
                parentEmail = parentEmail,
                childName = childName,
                status = ParentalConsentState.PENDING,
                timestamp = System.currentTimeMillis(),
                expiryDate = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000), // 7 days
                consentToken = generateConsentToken()
            )
            
            // Store request in Firestore
            firestore.collection("parental_consent_requests")
                .document(requestId)
                .set(request)
                .await()
            
            // Send consent email to parent
            sendConsentEmailToParent(request)
            
            Result.success(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Verify parental consent token
     */
    suspend fun verifyConsentToken(
        token: String,
        parentEmail: String,
        granted: Boolean,
        parentName: String
    ): Result<Unit> {
        return try {
            // Find the request by token
            val query = firestore.collection("parental_consent_requests")
                .whereEqualTo("consentToken", token)
                .whereEqualTo("parentEmail", parentEmail)
                .get()
                .await()
            
            val request = query.documents.firstOrNull()?.toObject(ParentalConsentRequest::class.java)
            
            if (request != null) {
                // Create consent record
                val consentRecord = ParentalConsentRecord(
                    id = "pc_${System.currentTimeMillis()}",
                    requestId = request.id,
                    userId = request.userId,
                    parentEmail = parentEmail,
                    parentName = parentName,
                    granted = granted,
                    timestamp = System.currentTimeMillis(),
                    ipAddress = getClientIpAddress(),
                    userAgent = getUserAgent()
                )
                
                firestore.collection("parental_consents")
                    .document(consentRecord.id)
                    .set(consentRecord)
                    .await()
                
                // Update request status
                firestore.collection("parental_consent_requests")
                    .document(request.id)
                    .update(mapOf(
                        "status" to if (granted) ParentalConsentState.GRANTED.name else ParentalConsentState.DENIED.name,
                        "processedAt" to System.currentTimeMillis()
                    ))
                    .await()
                
                if (granted) {
                    // Store consent locally
                    encryptedPrefs.edit()
                        .putBoolean(KEY_PARENTAL_CONSENT, true)
                        .putString(KEY_PARENTAL_CONSENT_ID, consentRecord.id)
                        .putLong(KEY_PARENTAL_CONSENT_TIMESTAMP, consentRecord.timestamp)
                        .apply()
                }
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("Invalid consent token"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Revoke parental consent
     */
    suspend fun revokeParentalConsent(userId: String, reason: String): Result<Unit> {
        return try {
            val consentId = encryptedPrefs.getString(KEY_PARENTAL_CONSENT_ID, null)
            
            if (consentId != null) {
                // Update consent record
                firestore.collection("parental_consents")
                    .document(consentId)
                    .update(mapOf(
                        "granted" to false,
                        "revoked" to true,
                        "revocationTimestamp" to System.currentTimeMillis(),
                        "revocationReason" to reason
                    ))
                    .await()
            }
            
            // Clear local consent
            encryptedPrefs.edit()
                .putBoolean(KEY_PARENTAL_CONSENT, false)
                .remove(KEY_PARENTAL_CONSENT_ID)
                .apply()
            
            _consentStatus.value = null
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get consent request status
     */
    suspend fun getConsentRequestStatus(requestId: String): Result<ParentalConsentRequest> {
        return try {
            val document = firestore.collection("parental_consent_requests")
                .document(requestId)
                .get()
                .await()
            
            if (document.exists()) {
                val request = document.toObject(ParentalConsentRequest::class.java)
                if (request != null) {
                    Result.success(request)
                } else {
                    Result.failure(Exception("Request not found"))
                }
            } else {
                Result.failure(Exception("Request not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if consent has expired
     */
    fun hasConsentExpired(): Boolean {
        val consentTimestamp = encryptedPrefs.getLong(KEY_PARENTAL_CONSENT_TIMESTAMP, 0L)
        val expiryTime = 365L * 24 * 60 * 60 * 1000 // 1 year
        return consentTimestamp > 0 && (System.currentTimeMillis() - consentTimestamp) > expiryTime
    }
    
    /**
     * Get age-appropriate content settings
     */
    fun getAgeAppropriateSettings(): AgeAppropriateSettings {
        return if (isUserUnder18()) {
            AgeAppropriateSettings(
                allowCrisisDetection = true,
                allowDirectHelplineAccess = true,
                allowDataSharing = false,
                allowPersonalization = true,
                contentFiltering = ContentFilteringLevel.STRICT,
                sessionDurationLimit = 30, // 30 minutes
                requireParentalApprovalForNewFeatures = true
            )
        } else {
            AgeAppropriateSettings(
                allowCrisisDetection = true,
                allowDirectHelplineAccess = true,
                allowDataSharing = true,
                allowPersonalization = true,
                contentFilteringLevel = ContentFilteringLevel.MODERATE,
                sessionDurationLimit = 60, // 60 minutes
                requireParentalApprovalForNewFeatures = false
            )
        }
    }
    
    private suspend fun sendConsentEmailToParent(request: ParentalConsentRequest) {
        // In production, this would integrate with an email service
        // For now, we'll store the email request in Firestore
        val emailRequest = mapOf(
            "to" to request.parentEmail,
            "subject" to "Parental Consent Request for DrMindit",
            "template" to "parental_consent_request",
            "data" to mapOf(
                "childName" to request.childName,
                "childEmail" to request.userEmail,
                "consentToken" to request.consentToken,
                "expiryDate" to request.expiryDate,
                "requestId" to request.id
            )
        )
        
        firestore.collection("email_queue")
            .add(emailRequest)
            .await()
    }
    
    private fun generateConsentToken(): String {
        return java.util.UUID.randomUUID().toString()
    }
    
    private fun getClientIpAddress(): String {
        return "0.0.0.0"
    }
    
    private fun getUserAgent(): String {
        return "DrMindit Android App v1.0"
    }
    
    companion object {
        private const val KEY_USER_UNDER_18 = "user_under_18"
        private const val KEY_BIRTH_DATE = "birth_date"
        private const val KEY_PARENTAL_CONSENT = "parental_consent"
        private const val KEY_PARENTAL_CONSENT_ID = "parental_consent_id"
        private const val KEY_PARENTAL_CONSENT_TIMESTAMP = "parental_consent_timestamp"
    }
}

/**
 * Data classes for parental consent
 */
data class ParentalConsentStatus(
    val userId: String,
    val isUnder18: Boolean,
    val consentRequired: Boolean,
    val consentGranted: Boolean,
    val consentId: String?,
    val parentEmail: String? = null,
    val consentTimestamp: Long? = null,
    val expiryDate: Long? = null,
    val status: ParentalConsentState
)

data class ParentalConsentRequest(
    val id: String,
    val userId: String,
    val userEmail: String,
    val parentEmail: String,
    val childName: String,
    val status: ParentalConsentState,
    val timestamp: Long,
    val expiryDate: Long,
    val consentToken: String,
    val processedAt: Long? = null
)

data class ParentalConsentRecord(
    val id: String,
    val requestId: String,
    val userId: String,
    val parentEmail: String,
    val parentName: String,
    val granted: Boolean,
    val timestamp: Long,
    val ipAddress: String,
    val userAgent: String,
    val revoked: Boolean = false,
    val revocationTimestamp: Long? = null,
    val revocationReason: String? = null
)

data class AgeAppropriateSettings(
    val allowCrisisDetection: Boolean,
    val allowDirectHelplineAccess: Boolean,
    val allowDataSharing: Boolean,
    val allowPersonalization: Boolean,
    val contentFilteringLevel: ContentFilteringLevel,
    val sessionDurationLimit: Int, // minutes
    val requireParentalApprovalForNewFeatures: Boolean
)

/**
 * Enums for parental consent
 */
enum class ParentalConsentState {
    NOT_REQUESTED, PENDING, GRANTED, DENIED, EXPIRED, REVOKED
}

enum class ContentFilteringLevel {
    NONE, LIGHT, MODERATE, STRICT
}
