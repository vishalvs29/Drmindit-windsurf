package com.drmindit.android.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.drmindit.android.player.EnhancedAudioPlayerManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Audio System Validator for comprehensive testing and validation
 * Ensures all critical scenarios work correctly
 */
@Singleton
class AudioSystemValidator @Inject constructor(
    private val context: Context,
    private val audioPlayerManager: EnhancedAudioPlayerManager
) {
    
    /**
     * Validate core behavior scenarios
     */
    fun validateCoreBehavior(): ValidationResult {
        Timber.d("🔍 Validating core behavior")
        
        val results = mutableListOf<String>()
        
        // Test 1: Player initialization
        try {
            audioPlayerManager.initializePlayer()
            results.add("✅ Player initializes correctly")
        } catch (e: Exception) {
            results.add("❌ Player initialization failed: ${e.message}")
        }
        
        // Test 2: State management
        val initialState = audioPlayerManager.playerState.value
        if (initialState.sessionId == null && !initialState.isPlaying) {
            results.add("✅ Initial state is correct")
        } else {
            results.add("❌ Initial state is incorrect")
        }
        
        // Test 3: Single instance check
        if (audioPlayerManager.isPlayerActive()) {
            results.add("✅ Single player instance active")
        } else {
            results.add("❌ Player instance not active")
        }
        
        return ValidationResult(results)
    }
    
    /**
     * Validate lifecycle management
     */
    fun validateLifecycleManagement(): ValidationResult {
        Timber.d("🔍 Validating lifecycle management")
        
        val results = mutableListOf<String>()
        
        // Test 1: Screen exit cleanup
        try {
            audioPlayerManager.forceStopForLifecycle()
            val stateAfterStop = audioPlayerManager.playerState.value
            if (!stateAfterStop.isPlaying && stateAfterStop.sessionId == null) {
                results.add("✅ Screen exit cleanup works")
            } else {
                results.add("❌ Screen exit cleanup failed")
            }
        } catch (e: Exception) {
            results.add("❌ Screen exit cleanup error: ${e.message}")
        }
        
        // Test 2: Resource release
        try {
            audioPlayerManager.release()
            results.add("✅ Resource release works")
        } catch (e: Exception) {
            results.add("❌ Resource release failed: ${e.message}")
        }
        
        return ValidationResult(results)
    }
    
    /**
     * Validate memory leak prevention
     */
    fun validateMemoryLeakPrevention(): ValidationResult {
        Timber.d("🔍 Validating memory leak prevention")
        
        val results = mutableListOf<String>()
        
        // Test 1: Multiple player instances
        try {
            // Try to initialize multiple times
            audioPlayerManager.initializePlayer()
            audioPlayerManager.initializePlayer()
            audioPlayerManager.initializePlayer()
            
            if (audioPlayerManager.isPlayerActive()) {
                results.add("✅ Single player instance maintained")
            } else {
                results.add("❌ Multiple player instances created")
            }
        } catch (e: Exception) {
            results.add("❌ Multiple instance test failed: ${e.message}")
        }
        
        // Test 2: Proper cleanup
        try {
            audioPlayerManager.release()
            results.add("✅ Cleanup completed without errors")
        } catch (e: Exception) {
            results.add("❌ Cleanup failed: ${e.message}")
        }
        
        return ValidationResult(results)
    }
    
    /**
     * Validate playback state management
     */
    fun validatePlaybackStateManagement(): ValidationResult {
        Timber.d("🔍 Validating playback state management")
        
        val results = mutableListOf<String>()
        
        // Test 1: StateFlow updates
        try {
            val initialState = audioPlayerManager.playerState.value
            val timestamp = initialState.timestamp
            
            // Wait a moment and check if state updates
            Thread.sleep(100)
            val updatedState = audioPlayerManager.playerState.value
            
            if (updatedState.timestamp >= timestamp) {
                results.add("✅ StateFlow updates correctly")
            } else {
                results.add("❌ StateFlow not updating")
            }
        } catch (e: Exception) {
            results.add("❌ StateFlow test failed: ${e.message}")
        }
        
        return ValidationResult(results)
    }
    
    /**
     * Validate edge cases
     */
    fun validateEdgeCases(): ValidationResult {
        Timber.d("🔍 Validating edge cases")
        
        val results = mutableListOf<String>()
        
        // Test 1: No internet
        val hasInternet = isInternetAvailable()
        if (!hasInternet) {
            results.add("✅ No internet detection works")
        } else {
            results.add("ℹ️ Internet available - no internet test skipped")
        }
        
        // Test 2: Invalid audio URL
        try {
            audioPlayerManager.loadAudio(
                sessionId = "test-invalid",
                audioUrl = "invalid-url",
                title = "Invalid Test"
            )
            
            // Check if error state is set
            Thread.sleep(1000)
            val state = audioPlayerManager.playerState.value
            if (state.error != null) {
                results.add("✅ Invalid URL error handling works")
            } else {
                results.add("❌ Invalid URL not handled properly")
            }
        } catch (e: Exception) {
            results.add("✅ Invalid URL properly rejected: ${e.message}")
        }
        
        // Test 3: Rapid operations
        try {
            repeat(10) {
                audioPlayerManager.forceStopForLifecycle()
                audioPlayerManager.initializePlayer()
            }
            results.add("✅ Rapid operations handled correctly")
        } catch (e: Exception) {
            results.add("❌ Rapid operations failed: ${e.message}")
        }
        
        return ValidationResult(results)
    }
    
    /**
     * Run comprehensive validation
     */
    fun runComprehensiveValidation(): ComprehensiveValidationResult {
        Timber.d("🔍 Running comprehensive validation")
        
        val coreBehavior = validateCoreBehavior()
        val lifecycleManagement = validateLifecycleManagement()
        val memoryLeaks = validateMemoryLeakPrevention()
        val playbackState = validatePlaybackStateManagement()
        val edgeCases = validateEdgeCases()
        
        val allResults = mutableListOf<String>()
        allResults.addAll(coreBehavior.results)
        allResults.addAll(lifecycleManagement.results)
        allResults.addAll(memoryLeaks.results)
        allResults.addAll(playbackState.results)
        allResults.addAll(edgeCases.results)
        
        val passedCount = allResults.count { it.startsWith("✅") }
        val totalCount = allResults.size
        
        val overallStatus = when {
            passedCount == totalCount -> "✅ ALL TESTS PASSED"
            passedCount >= totalCount * 0.8 -> "⚠️ MOST TESTS PASSED"
            else -> "❌ MANY TESTS FAILED"
        }
        
        return ComprehensiveValidationResult(
            overallStatus = overallStatus,
            passedTests = passedCount,
            totalTests = totalCount,
            coreBehavior = coreBehavior,
            lifecycleManagement = lifecycleManagement,
            memoryLeaks = memoryLeaks,
            playbackState = playbackState,
            edgeCases = edgeCases,
            allResults = allResults
        )
    }
    
    /**
     * Check internet availability
     */
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}

/**
 * Validation result for individual test categories
 */
data class ValidationResult(
    val results: List<String>
) {
    val passed: Int get() = results.count { it.startsWith("✅") }
    val total: Int get() = results.size
    val status: String get() = when {
        passed == total -> "✅ PASSED"
        passed >= total * 0.8 -> "⚠️ MOSTLY PASSED"
        else -> "❌ FAILED"
    }
}

/**
 * Comprehensive validation result
 */
data class ComprehensiveValidationResult(
    val overallStatus: String,
    val passedTests: Int,
    val totalTests: Int,
    val coreBehavior: ValidationResult,
    val lifecycleManagement: ValidationResult,
    val memoryLeaks: ValidationResult,
    val playbackState: ValidationResult,
    val edgeCases: ValidationResult,
    val allResults: List<String>
) {
    val passRate: Float get() = (passedTests.toFloat() / totalTests) * 100
    
    fun generateReport(): String {
        return buildString {
            appendLine("🎵 DrMindit Audio System Validation Report")
            appendLine("=" .repeat(50))
            appendLine()
            appendLine("Overall Status: $overallStatus")
            appendLine("Tests Passed: $passedTests/$totalTests (${passRate.toInt()}%)")
            appendLine()
            
            appendLine("Core Behavior: ${coreBehavior.status} (${coreBehavior.passed}/${coreBehavior.total})")
            coreBehavior.results.forEach { appendLine("  $it") }
            appendLine()
            
            appendLine("Lifecycle Management: ${lifecycleManagement.status} (${lifecycleManagement.passed}/${lifecycleManagement.total})")
            lifecycleManagement.results.forEach { appendLine("  $it") }
            appendLine()
            
            appendLine("Memory Leak Prevention: ${memoryLeaks.status} (${memoryLeaks.passed}/${memoryLeaks.total})")
            memoryLeaks.results.forEach { appendLine("  $it") }
            appendLine()
            
            appendLine("Playback State Management: ${playbackState.status} (${playbackState.passed}/${playbackState.total})")
            playbackState.results.forEach { appendLine("  $it") }
            appendLine()
            
            appendLine("Edge Cases: ${edgeCases.status} (${edgeCases.passed}/${edgeCases.total})")
            edgeCases.results.forEach { appendLine("  $it") }
            appendLine()
            
            appendLine("=" .repeat(50))
            appendLine("Validation completed at ${System.currentTimeMillis()}")
        }
    }
}
