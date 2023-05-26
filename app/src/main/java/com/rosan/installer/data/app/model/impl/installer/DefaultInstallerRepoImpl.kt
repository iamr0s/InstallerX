package com.rosan.installer.data.app.model.impl.installer

import android.os.IBinder
import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.util.sourcePath
import com.rosan.installer.data.recycle.model.entity.DefaultPrivilegedService
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

object DefaultInstallerRepoImpl : IBinderInstallerRepoImpl() {
    override suspend fun iBinderWrapper(iBinder: IBinder): IBinder = iBinder

    override suspend fun onDeleteWork(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity
    ) {
        DefaultPrivilegedService().delete(entities.sourcePath())
    }
}