package com.rosan.installer.data.app.model.entity

import android.graphics.drawable.Drawable
import com.rosan.installer.data.app.model.entity.serializer.DrawableSerializer
import kotlinx.serialization.Serializable

@Serializable
sealed class AppEntity {
    abstract val packageName: String

    @Serializable
    data class MainEntity(
        val data: DataEntity,
        override val packageName: String,
        val versionCode: Long,
        val versionName: String,
        val label: String?,
        @Serializable(with = DrawableSerializer::class)
        val icon: Drawable?
    ) : AppEntity()

    @Serializable
    data class SplitEntity(
        val data: DataEntity,
        override val packageName: String,
        val splitName: String,
    ) : AppEntity()
}
