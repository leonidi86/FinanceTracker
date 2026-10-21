package com.example.financetracker

import android.app.Application
import com.example.financetracker.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FinanceTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FinanceTrackerApplication)
            modules(appModules)
        }
    }
}
