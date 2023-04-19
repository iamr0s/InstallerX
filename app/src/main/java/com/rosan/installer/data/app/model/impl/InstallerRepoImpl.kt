package com.rosan.installer.data.app.model.impl

import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.model.impl.installer.ConsoleInstallerRepo
import com.rosan.installer.data.app.model.impl.installer.DefaultInstallerRepoImpl
import com.rosan.installer.data.app.model.impl.installer.DhizukuInstallerRepoImpl
import com.rosan.installer.data.app.model.impl.installer.ShizukuInstallerRepoImpl
import com.rosan.installer.data.app.repo.InstallerRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

class InstallerRepoImpl : InstallerRepo {
    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity
    ) {
        val repo = when (config.authorizer) {
            ConfigEntity.Authorizer.Root -> ConsoleInstallerRepo()
            ConfigEntity.Authorizer.Shizuku -> ShizukuInstallerRepoImpl()
            ConfigEntity.Authorizer.Dhizuku -> DhizukuInstallerRepoImpl()
            ConfigEntity.Authorizer.Customize -> ConsoleInstallerRepo()
            else -> DefaultInstallerRepoImpl()
        }
        repo.doWork(config, entities, extra)
    }
}