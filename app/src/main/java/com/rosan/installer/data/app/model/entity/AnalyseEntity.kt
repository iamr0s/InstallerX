package com.rosan.installer.data.app.model.entity

import com.rosan.installer.data.app.util.DataType
import kotlinx.serialization.Serializable

@Serializable
data class AnalyseEntity(
    val data: DataEntity,
    val type: DataType
) {
}