package com.rosan.installer.ui.page.settings.config.all

import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

sealed class AllViewEvent {
    data class DeletedConfig(val configEntity: ConfigEntity) : AllViewEvent()
}
