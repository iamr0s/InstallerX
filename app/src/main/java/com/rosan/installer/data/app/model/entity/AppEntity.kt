package com.rosan.installer.data.app.model.entity

import android.graphics.drawable.Drawable

sealed class AppEntity {
    abstract val packageName: String

    abstract val name: String

    data class BaseEntity(
        override val packageName: String,
        val data: DataEntity,
        val versionCode: Long,
        val versionName: String,
        val label: String?,
        val icon: Drawable?
    ) : AppEntity() {
        override val name = "base.apk"
    }

    data class SplitEntity(
        override val packageName: String,
        val data: DataEntity,
        val splitName: String
    ) : AppEntity() {
        override val name = "$splitName.apk"
    }

    data class DexMetadataEntity(
        override val packageName: String,
        val data: DataEntity,
        val dmName: String
    ) : AppEntity() {
        override val name = "base.dm"
    }
}