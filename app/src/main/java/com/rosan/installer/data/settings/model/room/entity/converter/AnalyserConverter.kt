package com.rosan.installer.data.settings.model.room.entity.converter

import androidx.room.TypeConverter
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

class AnalyserConverter {
    @TypeConverter
    fun revert(value: String): ConfigEntity.Analyser =
        ConfigEntity.Analyser.values().find { it.value == value }
            ?: ConfigEntity.Analyser.R0s

    @TypeConverter
    fun convert(value: ConfigEntity.Analyser): String = value.value
}