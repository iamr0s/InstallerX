package com.rosan.installer.data.app.repo

import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

interface InstallerRepo {
    suspend fun doWork(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity
    )
}