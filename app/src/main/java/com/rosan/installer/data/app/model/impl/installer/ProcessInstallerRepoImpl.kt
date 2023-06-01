package com.rosan.installer.data.app.model.impl.installer

import android.os.IBinder
import com.rosan.app_process.AppProcess
import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.util.sourcePath
import com.rosan.installer.data.recycle.model.impl.AppProcessRecyclers
import com.rosan.installer.data.recycle.repo.Recyclable
import com.rosan.installer.data.recycle.util.useUserService
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

object ProcessInstallerRepoImpl : IBinderInstallerRepoImpl() {
    private lateinit var recycler: Recyclable<AppProcess>

    override suspend fun doWork(
        config: ConfigEntity, entities: List<InstallEntity>, extra: InstallExtraEntity
    ) {
        recycler = AppProcessRecyclers.get(
            if (config.authorizer == ConfigEntity.Authorizer.Root) "su"
            else config.customizeAuthorizer
        ).make()
        super.doWork(config, entities, extra)
    }

    override suspend fun iBinderWrapper(iBinder: IBinder): IBinder =
        recycler.entity.binderWrapper(iBinder)

    override suspend fun doFinishWork(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity,
        result: Result<Unit>
    ) {
        super.doFinishWork(config, entities, extra, result)
        recycler.recycle()
    }
}