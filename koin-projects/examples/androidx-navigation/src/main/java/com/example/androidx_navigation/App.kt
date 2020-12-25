package com.example.androidx_navigation

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.module

// This is a global dependency that should be injectable from anywhere
class AppScopeDep

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(module { single { AppScopeDep() } })
        }
    }
}
