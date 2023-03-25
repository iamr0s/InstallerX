package com.rosan.installer.data.app.model.impl

import com.rosan.installer.data.app.model.entity.AnalyseEntity
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.impl.analyser.AuthorizerAnalyserRepoImpl
import com.rosan.installer.data.app.model.impl.analyser.ProcessAnalyserRepoImpl
import com.rosan.installer.data.app.repo.AnalyserRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

class AnalyserRepoImpl : AnalyserRepo {
    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<AnalyseEntity>
    ): List<AppEntity> = if (config.compatMode) ProcessAnalyserRepoImpl().doWork(config, entities)
    else AuthorizerAnalyserRepoImpl().doWork(config, entities)
}