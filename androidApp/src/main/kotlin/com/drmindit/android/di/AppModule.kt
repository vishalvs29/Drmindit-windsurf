package com.drmindit.android.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.drmindit.android.data.local.ChatDatabase
import com.drmindit.android.data.local.ChatLocalDataSourceImpl
import com.drmindit.android.data.local.provideChatDatabase
import com.drmindit.android.data.local.provideChatDataStore
import com.drmindit.android.data.repository.UserRepositoryImpl
import com.drmindit.android.data.repository.SessionRepositoryImpl
import com.drmindit.android.data.repository.ProgramRepositoryImpl
import com.drmindit.android.data.remote.MockRemoteDataSource
import com.drmindit.android.data.local.MockLocalDataSource
import com.drmindit.android.player.AudioPlayerController
import com.drmindit.android.player.AudioPlayerManager
import com.drmindit.android.crisis.CrisisDetector
import com.drmindit.android.crisis.CrisisEscalationManager
import com.drmindit.android.ui.viewmodel.*
import com.drmindit.shared.domain.repository.*
import com.drmindit.shared.domain.usecase.*
import com.drmindit.shared.data.remote.RemoteDataSource
import com.drmindit.shared.data.local.LocalDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    
    // Context
    single { androidContext() }
    
    // DataStore
    single<DataStore<Preferences>> { provideChatDataStore(get()) }
    
    // Database
    single { provideChatDatabase(get()) }
    
    // DAO
    single { get<ChatDatabase>().chatDao() }
    
    // Data Sources
    single<RemoteDataSource> { MockRemoteDataSource() }
    single<LocalDataSource> { MockLocalDataSource() }
    single<ChatLocalDataSource> { 
        ChatLocalDataSourceImpl(
            context = get(),
            database = get(),
            dataStore = get()
        ) 
    }
    
    // Remote Data Sources (placeholders - implement based on your backend)
    single<ChatRemoteDataSource> { 
        // TODO: Implement actual remote data source
        error("Remote data source not implemented yet") 
    }
    
    single<NotificationRemoteDataSource> { 
        // TODO: Implement actual remote data source
        error("Remote data source not implemented yet") 
    }
    
    single<AudioSessionRemoteDataSource> { 
        // TODO: Implement actual remote data source
        error("Remote data source not implemented yet") 
    }
    
    // Repositories
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<SessionRepository> { SessionRepositoryImpl(get(), get()) }
    single<ProgramRepository> { ProgramRepositoryImpl(get(), get()) }
    single<ChatRepository> { 
        com.drmindit.shared.data.repository.ChatRepositoryImpl(
            localDataSource = get(),
            remoteDataSource = get()
        )
    }
    
    single<NotificationRepository> { 
        com.drmindit.shared.data.repository.NotificationRepositoryImpl(
            remoteDataSource = get()
        )
    }
    
    single<AudioSessionRepository> { 
        com.drmindit.shared.data.repository.AudioSessionRepositoryImpl(
            remoteDataSource = get()
        )
    }
    
    // Use Cases
    single { GetUserUseCase(get()) }
    single { GetSessionsUseCase(get()) }
    single { GetSessionOfTheDayUseCase(get()) }
    single { SearchSessionsUseCase(get()) }
    
    // Audio Player
    single { AudioPlayerManager(get()) }
    single { AudioPlayerController(get()) }
    
    // Crisis Management
    single { CrisisDetector() }
    single { CrisisEscalationManager(get(), get()) }
    
    // ViewModels
    viewModel { DashboardViewModel(get(), get()) }
    viewModel { SessionPlayerViewModel() }
    viewModel { LibraryViewModel(get(), get(), get()) }
    viewModel { ProgramDetailViewModel() }
    viewModel { OnboardingViewModel() }
    viewModel { OrganizationDashboardViewModel() }
    viewModel { ChatViewModel(get(), get(), get()) }
    viewModel { NotificationSettingsViewModel(get()) }
    viewModel { AudioPlayerViewModel(get(), get()) }
}
