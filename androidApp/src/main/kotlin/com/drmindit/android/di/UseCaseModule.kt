package com.drmindit.android.di

import com.drmindit.shared.domain.usecase.*
import com.drmindit.shared.data.usecase.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    @Singleton
    abstract fun bindGetUserUseCase(
        getUserUseCase: GetUserUseCase
    ): GetUserUseCase

    @Binds
    @Singleton
    abstract fun bindGetSessionsUseCase(
        getSessionsUseCase: GetSessionsUseCase
    ): GetSessionsUseCase

    @Binds
    @Singleton
    abstract fun bindGetSessionOfTheDayUseCase(
        getSessionOfTheDayUseCase: GetSessionOfTheDayUseCase
    ): GetSessionOfTheDayUseCase

    @Binds
    @Singleton
    abstract fun bindSearchSessionsUseCase(
        searchSessionsUseCase: SearchSessionsUseCase
    ): SearchSessionsUseCase
}
