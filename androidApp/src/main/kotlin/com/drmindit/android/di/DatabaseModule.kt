package com.drmindit.android.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.work.WorkManager
import com.drmindit.android.data.local.ChatDatabase
import com.drmindit.android.data.local.ChatLocalDataSourceImpl
import com.drmindit.android.data.local.provideChatDatabase
import com.drmindit.android.data.local.provideChatDataStore
import com.drmindit.android.player.EnhancedAudioPlayerManager
import com.drmindit.android.notifications.NotificationManager
import com.drmindit.android.ai.MentalHealthChatManager
import com.drmindit.shared.data.local.LocalDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Managers
    @Provides
    @Singleton
    fun provideEnhancedAudioPlayerManager(@ApplicationContext context: Context): EnhancedAudioPlayerManager {
        return EnhancedAudioPlayerManager(context)
    }

    @Provides
    @Singleton
    fun provideMentalHealthChatManager(
        @ApplicationContext context: Context,
        workManager: WorkManager,
        dataStore: DataStore<Preferences>,
        chatDatabase: ChatDatabase
    ): MentalHealthChatManager {
        return MentalHealthChatManager(context, workManager, dataStore, chatDatabase)
    }

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        workManager: WorkManager,
        chatDatabase: ChatDatabase
    ): NotificationManager {
        return NotificationManager(context, workManager, chatDatabase)
    }

    @Provides
    @Singleton
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase {
        return provideChatDatabase(context)
    }

    @Provides
    @Singleton
    fun provideChatDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return provideChatDataStore(context)
    }

    @Binds
    @Singleton
    fun bindChatLocalDataSource(
        chatLocalDataSourceImpl: ChatLocalDataSourceImpl
    ): LocalDataSource {
        return chatLocalDataSourceImpl
    }
}
