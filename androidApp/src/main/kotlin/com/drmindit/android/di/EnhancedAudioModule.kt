package com.drmindit.android.di

import android.content.Context
import com.drmindit.android.player.EnhancedAudioPlayerManager
import com.drmindit.android.player.EnhancedAudioService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Enhanced Audio Module with proper dependency injection
 * Ensures single instances and proper lifecycle management
 */
@Module
@InstallIn(SingletonComponent::class)
object EnhancedAudioModule {
    
    @Provides
    @Singleton
    fun provideEnhancedAudioPlayerManager(
        @ApplicationContext context: Context
    ): EnhancedAudioPlayerManager {
        return EnhancedAudioPlayerManager(context)
    }
    
    @Provides
    @Singleton
    fun provideAudioService(): Class<EnhancedAudioService> {
        return EnhancedAudioService::class.java
    }
}
