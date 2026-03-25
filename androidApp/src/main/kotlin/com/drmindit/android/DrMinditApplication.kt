package com.drmindit.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DrMinditApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}
