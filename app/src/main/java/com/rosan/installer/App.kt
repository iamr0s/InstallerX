package com.rosan.installer

import android.app.Application
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.installer.di.init.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import rikka.sui.Sui

class App : Application() {
    override fun onCreate() {
        CrashHandler.init()
        super.onCreate()
        startKoin {
            // Koin Android Logger
            androidLogger()
            // Koin Android Context
            androidContext(this@App)
            // use modules
            modules(appModules)
        }
        Sui.init(BuildConfig.APPLICATION_ID)
        Dhizuku.init()
    }
}
