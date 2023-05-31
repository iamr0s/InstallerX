package com.rosan.installer.data.settings.model.room.entity.converter

import androidx.room.TypeConverter
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

object InstallModeConverter {
    @TypeConverter
    fun revert(value: String): ConfigEntity.InstallMode =
        ConfigEntity.InstallMode.values().find { it.value == value }
            ?: ConfigEntity.InstallMode.Dialog

    @TypeConverter
    fun convert(value: ConfigEntity.InstallMode): String = value.value
}