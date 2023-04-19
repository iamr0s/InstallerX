package com.rosan.installer.data.app.model.entity

import android.graphics.drawable.Drawable
import com.rosan.installer.data.app.model.entity.serializer.DrawableSerializer
import kotlinx.serialization.Serializable

@Serializable
sealed class AppEntity {
    abstract val packageName: String

    abstract val name: String

    @Serializable
    data class BaseEntity(
        override val packageName: String,
        val data: DataEntity,
        val versionCode: Long,
        val versionName: String,
        val label: String?,
        @Serializable(with = DrawableSerializer::class)
        val icon: Drawable?
    ) : AppEntity() {
        override val name = "base.apk"
    }

    @Serializable
    data class SplitEntity(
        override val packageName: String,
        val data: DataEntity,
        val splitName: String
    ) : AppEntity() {
        override val name = "$splitName.apk"
    }

    @Serializable
    data class DexMetadataEntity(
        override val packageName: String,
        val data: DataEntity,
        val dmName: String
    ) : AppEntity() {
        override val name = "base.dm"
    }
}