package com.drmindit.android

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DrMinditApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}
