package com.rosan.installer.data.app.model.impl.installer

import android.content.Context
import android.os.IBinder
import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.util.sourcePath
import com.rosan.installer.data.recycle.model.impl.ShizukuPrivilegedServiceRecycler
import com.rosan.installer.data.recycle.util.requireShizukuPermissionGranted
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import rikka.shizuku.ShizukuBinderWrapper

object ShizukuInstallerRepoImpl : IBinderInstallerRepoImpl(), KoinComponent {
    private val context by inject<Context>()

    override suspend fun iBinderWrapper(iBinder: IBinder): IBinder =
        requireShizukuPermissionGranted {
            ShizukuBinderWrapper(iBinder)
        }

    override suspend fun onDeleteWork(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity
    ) {
        ShizukuPrivilegedServiceRecycler.make().use {
            it.entity.delete(entities.sourcePath())
        }
    }
}