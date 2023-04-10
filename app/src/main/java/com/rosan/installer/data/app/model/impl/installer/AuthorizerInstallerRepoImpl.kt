package com.rosan.installer.data.app.model.impl.installer

import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.repo.installer.ConsoleInstallerRepo
import com.rosan.installer.data.console.repo.ConsoleRepo
import com.rosan.installer.data.console.util.ConsoleRepoUtil
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

class AuthorizerInstallerRepoImpl : ConsoleInstallerRepo {
    override suspend fun loadConsole(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity
    ): ConsoleRepo = when (config.authorizer) {
        ConfigEntity.Authorizer.None -> ConsoleRepoUtil.sh { }
        ConfigEntity.Authorizer.Root -> ConsoleRepoUtil.su { }
        ConfigEntity.Authorizer.Shizuku -> ConsoleRepoUtil.shizuku { }
        ConfigEntity.Authorizer.Dhizuku -> ConsoleRepoUtil.dhizuku { }
        ConfigEntity.Authorizer.Customize -> ConsoleRepoUtil.open {
            this.command(config.customizeAuthorizer)
        }
    }
}