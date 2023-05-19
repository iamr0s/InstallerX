package com.rosan.installer.data.app.model.impl.installer

import android.content.Context
import android.util.Log
import com.rosan.app_process.AppProcess
import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.PrintWriter
import java.util.StringTokenizer
import kotlin.concurrent.thread

object CustomizeInstallerRepoImpl : AppProcessInstallerRepoImpl(), KoinComponent {
    private val context by inject<Context>()

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

        override fun start(
            classPath: String,
            entryClassName: String,
            args: MutableList<String>
        ): Process {
            return super.start(classPath, entryClassName, args).apply {
                Log.e("r0s", "process $this")
                thread {
                    kotlin.runCatching {
                        Log.e("r0s", "code ${this.waitFor()}")
                    }
                }
                thread {
                    kotlin.runCatching {
                        while (this.isAlive) {
                            val len = this.inputStream.available()
                            if (len > 0) {
                                val bytes = ByteArray(len);
                                this.inputStream.read(bytes)
                                Log.e("r0s", "in ${bytes.decodeToString()}")
                            }
                        }
                    }
                }
                thread {
                    kotlin.runCatching {
                        while (this.isAlive) {
                            val len = this.errorStream.available()
                            if (len > 0) {
                                val bytes = ByteArray(len);
                                this.errorStream.read(bytes)
                                Log.e("r0s", "in ${bytes.decodeToString()}")
                            }
                        }
                    }
                }
            }
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