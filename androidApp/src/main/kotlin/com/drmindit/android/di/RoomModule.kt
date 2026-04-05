package com.drmindit.android.di

import android.content.Context
import androidx.room.Room
import com.drmindit.android.data.database.AppDatabase
import com.drmindit.android.data.database.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "drmindit_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideSessionDao(database: AppDatabase): SessionDao {
        return database.sessionDao()
    }
}
