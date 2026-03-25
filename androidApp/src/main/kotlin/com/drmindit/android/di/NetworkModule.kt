package com.drmindit.android.di

import com.drmindit.android.data.remote.MockRemoteDataSource
import com.drmindit.android.data.local.MockLocalDataSource
import com.drmindit.shared.data.remote.RemoteDataSource
import com.drmindit.shared.data.local.LocalDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Binds
    @Singleton
    fun bindRemoteDataSource(
        mockRemoteDataSource: MockRemoteDataSource
    ): RemoteDataSource {
        return mockRemoteDataSource
    }

    @Binds
    @Singleton
    fun bindLocalDataSource(
        mockLocalDataSource: MockLocalDataSource
    ): LocalDataSource {
        return mockLocalDataSource
    }
}
