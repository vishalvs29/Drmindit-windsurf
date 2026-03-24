package com.drmindit.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import com.drmindit.android.di.appModule

class DrMinditApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Koin DI
        startKoin {
            androidLogger()
            androidContext(this@DrMinditApplication)
            modules(appModule)
        }
    }
}
