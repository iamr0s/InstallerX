package com.rosan.installer.data.installer.repo

import android.app.Activity
import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.installer.model.entity.InstallEntity
import com.rosan.installer.data.installer.model.entity.ProgressEntity
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import kotlinx.coroutines.flow.Flow
import java.io.Closeable

interface InstallerRepo : Closeable {
    val id: String

    var error: Throwable

    var config: ConfigEntity

    var data: List<DataEntity>

    var entities: List<InstallEntity>

    val progress: Flow<ProgressEntity>

    val background: Flow<Boolean>

    fun resolve(activity: Activity)

    fun analyse()

    fun install()

    fun background(value: Boolean)

    override fun close()
}