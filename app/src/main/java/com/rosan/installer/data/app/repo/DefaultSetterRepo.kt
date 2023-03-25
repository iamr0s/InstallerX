package com.rosan.installer.data.app.repo

import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

typealias DSRepo = DefaultSetterRepo

interface DefaultSetterRepo {
    suspend fun doWork(
        config: ConfigEntity,
        packageName: String,
        className: String,
        enabled: Boolean
    )
}