package com.rosan.installer.data.app.model.impl.installer

import android.content.ComponentName
import android.content.Context
import android.os.IBinder
import com.rosan.app_process.AppProcess
import com.rosan.installer.IPrivilegedService
import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.model.impl.privileged.PrivilegedServiceImpl
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class AppProcessInstallerRepoImpl : IBinderInstallerRepoImpl(), KoinComponent {
    private val context by inject<Context>()

    private lateinit var process: AppProcess

    private var privileged: IPrivilegedService? = null

    abstract fun createAppProcess(
        config: ConfigEntity, entities: List<InstallEntity>, extra: InstallExtraEntity
    ): AppProcess

    override suspend fun doWork(
        config: ConfigEntity, entities: List<InstallEntity>, extra: InstallExtraEntity
    ) {
        process = createAppProcess(config, entities, extra)
        super.doWork(config, entities, extra)
    }

    override suspend fun iBinderWrapper(iBinder: IBinder): IBinder = process.binderWrapper(iBinder)

    override suspend fun doDeleteWork(path: String) {
        if (privileged == null) privileged = IPrivilegedService.Stub.asInterface(
            process.startProcess(
                ComponentName(
                    context,
                    PrivilegedServiceImpl::class.java
                )
            )
        )
        privileged?.deletePath(path)
    }

    override suspend fun doFinishWork(
        config: ConfigEntity, entities: List<InstallEntity>, extra: InstallExtraEntity
    ) {
        super.doFinishWork(config, entities, extra)
        process.close()
    }
}