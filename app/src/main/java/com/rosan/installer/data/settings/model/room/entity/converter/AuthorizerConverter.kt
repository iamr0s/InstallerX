package com.rosan.installer.data.settings.model.room.entity.converter

import androidx.room.TypeConverter
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

class AuthorizerConverter {
    @TypeConverter
    fun revert(value: String): ConfigEntity.Authorizer =
        ConfigEntity.Authorizer.values().find { it.value == value }
            ?: ConfigEntity.Authorizer.Shizuku

    @TypeConverter
    fun convert(value: ConfigEntity.Authorizer): String = value.value
}