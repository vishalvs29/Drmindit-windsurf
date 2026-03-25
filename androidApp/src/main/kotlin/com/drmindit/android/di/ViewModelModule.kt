package com.drmindit.android.di

import com.drmindit.android.ui.viewmodel.*
import com.drmindit.android.player.AudioPlayerController
import com.drmindit.android.crisis.CrisisDetector
import com.drmindit.android.crisis.CrisisEscalationManager
import com.drmindit.android.compliance.DPDPComplianceManager
import com.drmindit.android.compliance.ParentalConsentManager
import com.drmindit.android.content.TraumaInformedContentManager
import com.drmindit.android.config.SecureConfigManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ViewModelModule {

    @Binds
    @Singleton
    abstract fun bindDashboardViewModel(
        dashboardViewModel: DashboardViewModel
    ): DashboardViewModel

    @Binds
    @Singleton
    abstract fun bindSessionPlayerViewModel(
        sessionPlayerViewModel: SessionPlayerViewModel
    ): SessionPlayerViewModel

    @Binds
    @Singleton
    abstract fun bindLibraryViewModel(
        libraryViewModel: LibraryViewModel
    ): LibraryViewModel

    @Binds
    @Singleton
    abstract fun bindProgramDetailViewModel(
        programDetailViewModel: ProgramDetailViewModel
    ): ProgramDetailViewModel

    @Binds
    @Singleton
    abstract fun bindOnboardingViewModel(
        onboardingViewModel: OnboardingViewModel
    ): OnboardingViewModel

    @Binds
    @Singleton
    abstract fun bindOrganizationDashboardViewModel(
        organizationDashboardViewModel: OrganizationDashboardViewModel
    ): OrganizationDashboardViewModel

    @Binds
    @Singleton
    abstract fun bindChatViewModel(
        chatViewModel: ChatViewModel
    ): ChatViewModel

    @Binds
    @Singleton
    abstract fun bindNotificationSettingsViewModel(
        notificationSettingsViewModel: NotificationSettingsViewModel
    ): NotificationSettingsViewModel

    @Binds
    @Singleton
    abstract fun bindImprovedAudioPlayerController(
        improvedAudioPlayerController: ImprovedAudioPlayerController
    ): ImprovedAudioPlayerController

    @Binds
    @Singleton
    abstract fun bindMentalHealthChatController(
        mentalHealthChatController: MentalHealthChatController
    ): MentalHealthChatController

    companion object {
        @Provides
        @Singleton
        @IntoMap
        fun provideCrisisDetectors(): Map<Class<*>, CrisisDetector> {
            return mapOf(
                CrisisDetector::class to CrisisDetector()
            )
        }

        @Provides
        @Singleton
        @IntoMap
        fun provideCrisisEscalationManagers(): Map<Class<*>, CrisisEscalationManager> {
            return mapOf(
                CrisisEscalationManager::class to CrisisEscalationManager()
            )
        }

        @Provides
        @Singleton
        @IntoMap
        fun provideDPDPComplianceManagers(): Map<Class<*>, DPDPComplianceManager> {
            return mapOf(
                DPDPComplianceManager::class to DPDPComplianceManager()
            )
        }

        @Provides
        @Singleton
        @IntoMap
        fun provideParentalConsentManagers(): Map<Class<*>, ParentalConsentManager> {
            return mapOf(
                ParentalConsentManager::class to ParentalConsentManager()
            )
        }

        @Provides
        @Singleton
        @IntoMap
        fun provideTraumaInformedContentManagers(): Map<Class<*>, TraumaInformedContentManager> {
            return mapOf(
                TraumaInformedContentManager::class to TraumaInformedContentManager()
            )
        }

        @Provides
        @Singleton
        @IntoMap
        fun provideSecureConfigManagers(): Map<Class<*>, SecureConfigManager> {
            return mapOf(
                SecureConfigManager::class to SecureConfigManager()
            )
        }
    }
}
