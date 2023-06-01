package com.rosan.installer.data.app.model.impl.installer

import android.os.IBinder
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.util.sourcePath
import com.rosan.installer.data.recycle.util.requireDhizukuPermissionGranted
import com.rosan.installer.data.recycle.util.useUserService
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import org.koin.core.component.KoinComponent

object DhizukuInstallerRepoImpl : IBinderInstallerRepoImpl(), KoinComponent {
    override suspend fun iBinderWrapper(iBinder: IBinder): IBinder =
        requireDhizukuPermissionGranted {
            Dhizuku.binderWrapper(iBinder)
        }

    override suspend fun onDeleteWork(
        config: ConfigEntity, entities: List<InstallEntity>, extra: InstallExtraEntity
    ) {
        useUserService(config, { null }) {
            it.privileged.delete(entities.sourcePath())
        }
    }
}