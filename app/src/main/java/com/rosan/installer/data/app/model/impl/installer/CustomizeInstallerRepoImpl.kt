package com.rosan.installer.data.app.model.impl.installer

import android.content.Context
import android.os.IBinder
import com.rosan.app_process.AppProcess
import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.PrintWriter
import java.util.StringTokenizer

object CustomizeInstallerRepoImpl : IBinderInstallerRepoImpl(), KoinComponent {
    private val context by inject<Context>()

    private lateinit var process: AppProcess

    class CustomizeAppProcess(private val shell: String) : AppProcess.Default() {
        override fun newProcess(params: ProcessParams): Process {
            val st = StringTokenizer(shell)
            val cmdList = mutableListOf<String>()
            while (st.hasMoreTokens()) {
                cmdList.add(st.nextToken());
            }
            val newPrams = ProcessParams(params)
            newPrams.cmdList = cmdList
            val process = super.newProcess(newPrams)
            PrintWriter(process.outputStream, true).also {
                it.println(params.cmdList.joinToString(" "))
                it.println("exit \$?")
            }
            return process
        }
    }

    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity
    ) {
        process = CustomizeAppProcess(config.customizeAuthorizer)
        process.init(context.packageName)
        try {
            super.doWork(config, entities, extra)
        } finally {
            process.close()
        }
    }

    override suspend fun iBinderWrapper(iBinder: IBinder): IBinder {
        return process.binderWrapper(iBinder)
    }
}