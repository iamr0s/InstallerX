package com.rosan.installer.di

import androidx.work.WorkManager
import org.koin.dsl.module

val workerModule = module {
    single<WorkManager> {
        WorkManager.getInstance(get())
    }
}