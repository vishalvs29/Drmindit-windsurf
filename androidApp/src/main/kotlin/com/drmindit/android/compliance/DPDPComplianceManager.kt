package com.drmindit.android.compliance

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

/**
 * DPDP Act 2023 Compliance Manager
 * Handles user consent, data access requests, and compliance features
 */
@Singleton
class DPDPComplianceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val encryptedPrefs: SharedPreferences
    
    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setKeySize(MasterKey.KEY_SIZE_256)
            .build()
        
        encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            "dpdp_compliance_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    private val _consentStatus = MutableStateFlow<ConsentStatus?>(null)
    val consentStatus: Flow<ConsentStatus?> = _consentStatus.asStateFlow()
    
    /**
     * Check if user has given consent
     */
    fun hasUserConsent(): Boolean {
        return encryptedPrefs.getBoolean(KEY_USER_CONSENT, false)
    }
    
    /**
     * Get consent timestamp
     */
    fun getConsentTimestamp(): Long {
        return encryptedPrefs.getLong(KEY_CONSENT_TIMESTAMP, 0L)
    }
    
    /**
     * Record user consent
     */
    suspend fun recordUserConsent(
        userId: String,
        consentType: ConsentType,
        dataCategories: List<DataCategory>,
        purposes: List<DataPurpose>
    ): Result<Unit> {
        return try {
            val consentRecord = ConsentRecord(
                id = "consent_${System.currentTimeMillis()}",
                userId = userId,
                consentType = consentType,
                dataCategories = dataCategories,
                purposes = purposes,
                timestamp = System.currentTimeMillis(),
                ipAddress = getClientIpAddress(),
                userAgent = getUserAgent(),
                version = "1.0"
            )
            
            // Store in Firestore
            firestore.collection("user_consents")
                .document(consentRecord.id)
                .set(consentRecord)
                .await()
            
            // Store locally
            encryptedPrefs.edit()
                .putBoolean(KEY_USER_CONSENT, true)
                .putLong(KEY_CONSENT_TIMESTAMP, consentRecord.timestamp)
                .putString(KEY_CONSENT_ID, consentRecord.id)
                .apply()
            
            _consentStatus.value = ConsentStatus(
                hasConsent = true,
                timestamp = consentRecord.timestamp,
                consentId = consentRecord.id,
                dataCategories = dataCategories,
                purposes = purposes
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Withdraw user consent
     */
    suspend fun withdrawConsent(userId: String, reason: String): Result<Unit> {
        return try {
            val consentId = encryptedPrefs.getString(KEY_CONSENT_ID, null)
            
            if (consentId != null) {
                // Update consent record
                firestore.collection("user_consents")
                    .document(consentId)
                    .update(mapOf(
                        "withdrawn" to true,
                        "withdrawalTimestamp" to System.currentTimeMillis(),
                        "withdrawalReason" to reason
                    ))
                    .await()
            }
            
            // Clear local consent
            encryptedPrefs.edit()
                .putBoolean(KEY_USER_CONSENT, false)
                .remove(KEY_CONSENT_ID)
                .apply()
            
            _consentStatus.value = null
            
            // Schedule data deletion (as per DPDP requirements)
            scheduleDataDeletion(userId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user's data access request
     */
    suspend fun getDataAccessRequest(userId: String): Result<UserDataAccessRequest> {
        return try {
            val request = UserDataAccessRequest(
                id = "dar_${System.currentTimeMillis()}",
                userId = userId,
                requestType = DataAccessRequestType.ACCESS,
                status = DataAccessRequestStatus.PENDING,
                timestamp = System.currentTimeMillis(),
                dataCategories = getAllDataCategories(userId)
            )
            
            firestore.collection("data_access_requests")
                .document(request.id)
                .set(request)
                .await()
            
            Result.success(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user's data deletion request
     */
    suspend fun getDataDeletionRequest(userId: String): Result<UserDataAccessRequest> {
        return try {
            val request = UserDataAccessRequest(
                id = "ddr_${System.currentTimeMillis()}",
                userId = userId,
                requestType = DataAccessRequestType.DELETION,
                status = DataAccessRequestStatus.PENDING,
                timestamp = System.currentTimeMillis(),
                dataCategories = getAllDataCategories(userId)
            )
            
            firestore.collection("data_access_requests")
                .document(request.id)
                .set(request)
                .await()
            
            Result.success(request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get privacy policy
     */
    suspend fun getPrivacyPolicy(): Result<PrivacyPolicy> {
        return try {
            val document = firestore.collection("legal_documents")
                .document("privacy_policy")
                .get()
                .await()
            
            val policy = PrivacyPolicy(
                id = document.id,
                version = document.getString("version") ?: "1.0",
                title = document.getString("title") ?: "Privacy Policy",
                content = document.getString("content") ?: "",
                lastUpdated = document.getLong("lastUpdated") ?: System.currentTimeMillis(),
                effectiveDate = document.getLong("effectiveDate") ?: System.currentTimeMillis()
            )
            
            Result.success(policy)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Log data processing activity
     */
    suspend fun logDataProcessing(
        userId: String,
        activity: DataProcessingActivity,
        dataCategory: DataCategory,
        purpose: DataPurpose
    ): Result<Unit> {
        return try {
            val logEntry = DataProcessingLog(
                id = "log_${System.currentTimeMillis()}",
                userId = userId,
                activity = activity,
                dataCategory = dataCategory,
                purpose = purpose,
                timestamp = System.currentTimeMillis(),
                legalBasis = LegalBasis.CONSENT
            )
            
            firestore.collection("data_processing_logs")
                .document(logEntry.id)
                .set(logEntry)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get consent status
     */
    suspend fun loadConsentStatus() {
        if (hasUserConsent()) {
            val consentId = encryptedPrefs.getString(KEY_CONSENT_ID, null)
            if (consentId != null) {
                try {
                    val document = firestore.collection("user_consents")
                        .document(consentId)
                        .get()
                        .await()
                    
                    if (document.exists()) {
                        _consentStatus.value = ConsentStatus(
                            hasConsent = true,
                            timestamp = document.getLong("timestamp") ?: 0L,
                            consentId = consentId,
                            dataCategories = document.get("dataCategories") as? List<DataCategory> ?: emptyList(),
                            purposes = document.get("purposes") as? List<DataPurpose> ?: emptyList()
                        )
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
    
    private suspend fun getAllDataCategories(userId: String): List<DataCategory> {
        return try {
            val documents = firestore.collection("user_data_categories")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            documents.mapNotNull { doc ->
                doc.getString("category")?.let { DataCategory.valueOf(it) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private suspend fun scheduleDataDeletion(userId: String) {
        // Schedule deletion after 30 days (as per DPDP)
        val deletionTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        
        firestore.collection("scheduled_deletions")
            .document(userId)
            .set(mapOf(
                "userId" to userId,
                "deletionTime" to deletionTime,
                "reason" to "consent_withdrawn"
            ))
    }
    
    private fun getClientIpAddress(): String {
        // In production, this would get the actual client IP
        return "0.0.0.0"
    }
    
    private fun getUserAgent(): String {
        return "DrMindit Android App v1.0"
    }
    
    companion object {
        private const val KEY_USER_CONSENT = "user_consent"
        private const val KEY_CONSENT_TIMESTAMP = "consent_timestamp"
        private const val KEY_CONSENT_ID = "consent_id"
    }
}

/**
 * Data classes for DPDP compliance
 */
data class ConsentRecord(
    val id: String,
    val userId: String,
    val consentType: ConsentType,
    val dataCategories: List<DataCategory>,
    val purposes: List<DataPurpose>,
    val timestamp: Long,
    val ipAddress: String,
    val userAgent: String,
    val version: String,
    val withdrawn: Boolean = false,
    val withdrawalTimestamp: Long? = null,
    val withdrawalReason: String? = null
)

data class ConsentStatus(
    val hasConsent: Boolean,
    val timestamp: Long,
    val consentId: String?,
    val dataCategories: List<DataCategory>,
    val purposes: List<DataPurpose>
)

data class UserDataAccessRequest(
    val id: String,
    val userId: String,
    val requestType: DataAccessRequestType,
    val status: DataAccessRequestStatus,
    val timestamp: Long,
    val dataCategories: List<DataCategory>,
    val processedAt: Long? = null,
    val notes: String? = null
)

data class PrivacyPolicy(
    val id: String,
    val version: String,
    val title: String,
    val content: String,
    val lastUpdated: Long,
    val effectiveDate: Long
)

data class DataProcessingLog(
    val id: String,
    val userId: String,
    val activity: DataProcessingActivity,
    val dataCategory: DataCategory,
    val purpose: DataPurpose,
    val timestamp: Long,
    val legalBasis: LegalBasis
)

/**
 * Enums for DPDP compliance
 */
enum class ConsentType {
    EXPLICIT, IMPLICIT, WITHDRAWN
}

enum class DataCategory {
    PERSONAL_INFO, HEALTH_DATA, USAGE_DATA, LOCATION_DATA, CONTACT_DATA
}

enum class DataPurpose {
    SERVICE_PROVISION, PERSONALIZATION, ANALYTICS, SAFETY_MONITORING, LEGAL_COMPLIANCE
}

enum class DataAccessRequestType {
    ACCESS, DELETION, CORRECTION, PORTABILITY
}

enum class DataAccessRequestStatus {
    PENDING, PROCESSING, COMPLETED, REJECTED
}

enum class DataProcessingActivity {
    COLLECTION, PROCESSING, STORAGE, SHARING, DELETION
}

enum class LegalBasis {
    CONSENT, CONTRACTUAL_OBLIGATION, LEGAL_COMPLIANCE, VITAL_INTERESTS
}
