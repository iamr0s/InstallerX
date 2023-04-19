package com.rosan.installer.data.app.model.impl.installer

import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.repo.InstallerRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

class ProcessInstallerRepoImpl : InstallerRepo {
    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity
    ) = DefaultInstallerRepoImpl().doWork(config, entities, extra)
}