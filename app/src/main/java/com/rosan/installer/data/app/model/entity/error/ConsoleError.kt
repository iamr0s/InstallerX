package com.rosan.installer.data.app.model.entity.error

import com.rosan.installer.data.common.model.entity.ErrorEntity
import kotlinx.serialization.Serializable

@Serializable
data class ConsoleError(
    val code: Int,
    val read: String,
    val error: String
) : ErrorEntity("")