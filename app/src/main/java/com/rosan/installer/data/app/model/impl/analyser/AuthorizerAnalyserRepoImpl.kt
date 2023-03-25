package com.rosan.installer.data.app.model.impl.analyser

import com.rosan.installer.data.app.model.entity.AnalyseEntity
import com.rosan.installer.data.app.repo.analyser.ConsoleAnalyserRepo
import com.rosan.installer.data.console.repo.ConsoleRepo
import com.rosan.installer.data.console.util.ConsoleRepoUtil
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

class AuthorizerAnalyserRepoImpl : ConsoleAnalyserRepo {
    override suspend fun loadConsole(
        config: ConfigEntity,
        entities: List<AnalyseEntity>,
    ): ConsoleRepo = when (config.authorizer) {
        ConfigEntity.Authorizer.None -> ConsoleRepoUtil.sh { }
        ConfigEntity.Authorizer.Root -> ConsoleRepoUtil.su { }
        ConfigEntity.Authorizer.Shizuku -> ConsoleRepoUtil.shizuku { }
        ConfigEntity.Authorizer.Customize -> ConsoleRepoUtil.open {
            this.command(config.customizeAuthorizer)
        }
    }
}