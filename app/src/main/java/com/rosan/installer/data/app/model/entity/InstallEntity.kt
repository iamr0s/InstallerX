package com.rosan.installer.data.app.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class InstallEntity(
    val name: String,
    val packageName: String,
    val data: DataEntity
)
