package com.rosan.installer.data.app.model.impl

import android.content.ComponentName
import android.content.Context
import com.rosan.installer.data.app.repo.DSRepo
import com.rosan.installer.data.recycle.model.impl.AppProcessPrivilegedServiceRecyclers
import com.rosan.installer.data.recycle.model.impl.DhizukuPrivilegedServiceRecycler
import com.rosan.installer.data.recycle.model.impl.ShizukuPrivilegedServiceRecycler
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import com.rosan.installer.ui.activity.InstallerActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object DSRepoImpl : DSRepo, KoinComponent {
    private val context by inject<Context>()

    override suspend fun doWork(config: ConfigEntity, enabled: Boolean) {
        when (config.authorizer) {
            ConfigEntity.Authorizer.Root -> AppProcessPrivilegedServiceRecyclers.get("su 1000")
                .make()

            ConfigEntity.Authorizer.Shizuku -> ShizukuPrivilegedServiceRecycler.make()
            ConfigEntity.Authorizer.Dhizuku -> DhizukuPrivilegedServiceRecycler.make()
            ConfigEntity.Authorizer.Customize -> AppProcessPrivilegedServiceRecyclers.get(config.customizeAuthorizer)
                .make()

            else -> AppProcessPrivilegedServiceRecyclers.get("sh").make()
        }.use {
            it.entity.setDefaultInstaller(
                ComponentName(context, InstallerActivity::class.java),
                enabled
            )
        }
    }
}