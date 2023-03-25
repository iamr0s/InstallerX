package com.rosan.installer.data.app.repo

import com.rosan.installer.data.app.model.entity.AnalyseEntity
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

interface AnalyserRepo {
    suspend fun doWork(
        config: ConfigEntity,
        entities: List<AnalyseEntity>
    ): List<AppEntity>
}