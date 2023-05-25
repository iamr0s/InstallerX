package com.rosan.installer.data.app.model.impl.installer

import android.content.Context
import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object RootInstallerRepoImpl : AppProcessInstallerRepoImpl(), KoinComponent {
    private val context by inject<Context>()
    override fun getShell(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity
    ): String = "su"
}