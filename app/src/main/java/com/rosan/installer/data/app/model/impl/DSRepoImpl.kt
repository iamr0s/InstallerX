package com.rosan.installer.data.app.model.impl

import android.content.ComponentName
import android.content.Context
import com.rosan.installer.data.app.repo.DSRepo
import com.rosan.installer.data.recycle.util.useUserService
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import com.rosan.installer.ui.activity.InstallerActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object DSRepoImpl : DSRepo, KoinComponent {
    private val context by inject<Context>()

    override suspend fun doWork(config: ConfigEntity, enabled: Boolean) {
        useUserService(
            config, if (config.authorizer == ConfigEntity.Authorizer.Root) {
                { "su 1000" }
            } else null
        ) {
            it.privileged
                .setDefaultInstaller(ComponentName(context, InstallerActivity::class.java), enabled)
        }
    }
}