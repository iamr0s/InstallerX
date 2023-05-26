package com.rosan.installer.data.app.model.impl.installer

import android.content.Context
import android.os.IBinder
import com.rosan.app_process.AppProcess
import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.util.sourcePath
import com.rosan.installer.data.recycle.model.impl.AppProcessPrivilegedServiceRecyclers
import com.rosan.installer.data.recycle.model.impl.AppProcessRecyclers
import com.rosan.installer.data.recycle.repo.Recyclable
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class AppProcessInstallerRepoImpl : IBinderInstallerRepoImpl(),
    KoinComponent {
    private val context by inject<Context>()

    private lateinit var recycler: Recyclable<AppProcess>

    abstract fun getShell(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity
    ): String

    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity
    ) {
        recycler = AppProcessRecyclers.get(getShell(config, entities, extra)).make()
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

    override suspend fun onDeleteWork(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity
    ) {
        AppProcessPrivilegedServiceRecyclers.get(getShell(config, entities, extra)).make()
            .use {
                it.entity.delete(entities.sourcePath())
            }
    }
}