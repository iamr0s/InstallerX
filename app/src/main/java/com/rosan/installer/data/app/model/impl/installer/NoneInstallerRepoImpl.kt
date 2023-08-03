package com.rosan.installer.data.app.model.impl.installer

import android.os.IBinder
import com.rosan.app_process.AppProcess
import com.rosan.app_process.NewProcessImpl
import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

object NoneInstallerRepoImpl : IBinderInstallerRepoImpl() {
    private val newProcess = NewProcessImpl();

    override suspend fun doWork(
        config: ConfigEntity, entities: List<InstallEntity>, extra: InstallExtraEntity
    ) {
        super.doWork(config, entities, extra)
    }

    override suspend fun iBinderWrapper(iBinder: IBinder): IBinder =
        AppProcess.binderWrapper(newProcess, iBinder)

    override suspend fun onDeleteWork(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity
    ) {
        super.onDeleteWork(config, entities, extra)
    }
}