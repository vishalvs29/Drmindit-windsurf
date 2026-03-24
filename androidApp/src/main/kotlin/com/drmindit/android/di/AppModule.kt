package com.drmindit.android.di

import com.drmindit.android.data.repository.UserRepositoryImpl
import com.drmindit.android.data.repository.SessionRepositoryImpl
import com.drmindit.android.data.repository.ProgramRepositoryImpl
import com.drmindit.android.data.remote.MockRemoteDataSource
import com.drmindit.android.data.local.MockLocalDataSource
import com.drmindit.android.ui.viewmodel.DashboardViewModel
import com.drmindit.android.ui.viewmodel.SessionPlayerViewModel
import com.drmindit.android.ui.viewmodel.LibraryViewModel
import com.drmindit.android.ui.viewmodel.ProgramDetailViewModel
import com.drmindit.android.ui.viewmodel.OnboardingViewModel
import com.drmindit.android.ui.viewmodel.OrganizationDashboardViewModel
import com.drmindit.shared.domain.repository.UserRepository
import com.drmindit.shared.domain.repository.SessionRepository
import com.drmindit.shared.domain.repository.ProgramRepository
import com.drmindit.shared.domain.usecase.GetUserUseCase
import com.drmindit.shared.domain.usecase.GetSessionsUseCase
import com.drmindit.shared.domain.usecase.GetSessionOfTheDayUseCase
import com.drmindit.shared.domain.usecase.SearchSessionsUseCase
import com.drmindit.shared.data.remote.RemoteDataSource
import com.drmindit.shared.data.local.LocalDataSource
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    
    // Data Sources
    single<RemoteDataSource> { MockRemoteDataSource() }
    single<LocalDataSource> { MockLocalDataSource() }
    
    // Repositories
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<SessionRepository> { SessionRepositoryImpl(get(), get()) }
    single<ProgramRepository> { ProgramRepositoryImpl(get(), get()) }
    
    // Use Cases
    single { GetUserUseCase(get()) }
    single { GetSessionsUseCase(get()) }
    single { GetSessionOfTheDayUseCase(get()) }
    single { SearchSessionsUseCase(get()) }
    
    // ViewModels
    viewModel { DashboardViewModel(get(), get()) }
    viewModel { SessionPlayerViewModel() }
    viewModel { LibraryViewModel(get(), get(), get()) }
    viewModel { ProgramDetailViewModel() }
    viewModel { OnboardingViewModel() }
    viewModel { OrganizationDashboardViewModel() }
}
