package com.drmindit.android.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.drmindit.android.data.local.ChatDatabase
import com.drmindit.android.data.local.ChatLocalDataSourceImpl
import com.drmindit.android.data.local.provideChatDatabase
import com.drmindit.android.data.local.provideChatDataStore
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
    fun provideImprovedAudioPlayerManager(): ImprovedAudioPlayerManager {
        return ImprovedAudioPlayerManager()
    }

    @Provides
    @Singleton
    fun provideAudioPlayerManager(improvedAudioPlayerManager: ImprovedAudioPlayerManager): AudioPlayerManager {
        return improvedAudioPlayerManager
    }

    @Provides
    @Singleton
    fun provideMentalHealthChatManager(): MentalHealthChatManager {
        return MentalHealthChatManager(get(), get(), get(), get())
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
