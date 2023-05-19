package com.rosan.installer.data.app.model.impl.installer

import android.content.Context
import com.rosan.app_process.AppProcess
import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.StringTokenizer

object CustomizeInstallerRepoImpl : AppProcessInstallerRepoImpl(), KoinComponent {
    private val context by inject<Context>()

    class CustomizeAppProcess(private val shell: String) : AppProcess.Terminal() {
        override fun newTerminal(): MutableList<String> {
            val st = StringTokenizer(shell)
            val cmdList = mutableListOf<String>()
            while (st.hasMoreTokens()) {
                cmdList.add(st.nextToken());
            }
            return cmdList;
        }
    }

    override fun createAppProcess(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity
    ): AppProcess = CustomizeAppProcess(config.customizeAuthorizer).apply {
        init(context.packageName)
    }
}