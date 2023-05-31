package com.rosan.installer.data.app.model.impl.installer

import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

object CustomizeInstallerRepoImpl : AppProcessInstallerRepoImpl() {
    override fun getShell(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity
    ): String = config.customizeAuthorizer
}