package com.rosan.installer.data.app.model.impl.installer

import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.repo.InstallerRepo
import com.rosan.installer.data.common.model.entity.ErrorEntity
import com.rosan.installer.data.common.util.addAll
import com.rosan.installer.data.console.util.ConsoleRepoUtil
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class ProcessInstallerRepoShellImpl : InstallerRepo {
    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity
    ) = withContext(Dispatchers.IO) {
        entities.groupBy { it.packageName }.values.forEach {
            doInnerWork(config, it, extra)
        }
    }

    private suspend fun doInnerWork(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity
    ) = withContext(Dispatchers.IO) {
        if (entities.isEmpty()) return@withContext
        val sessionId =
            createSessionId(config, entities, extra) ?: throw ErrorEntity("create session failed")
        if (!installIts(config, entities, extra, sessionId)) throw ErrorEntity("install failed")
        commit(config, entities, extra, sessionId)
    }

    private suspend fun createSessionId(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity
    ): String? = withContext(Dispatchers.IO) {
        if (entities.count { it is AppEntity.MainEntity } > 1) throw ErrorEntity("more than one main apk")
        val console = ConsoleRepoUtil.open {
            command = mutableListOf<String>().also {
                it.addAll("pm", "install-create")

                // 申明安装者
                config.installer?.let { installer ->
                    it.addAll("-i", "'$installer'")
                }

                // allow testOnly
                if (config.allowTestOnly) it.add("-t")

                // allow downgrade
                if (config.allowDowngrade) it.add("-d")

                /*
                 * ALL: -1
                 * CURRENT: -2
                 * @see android.os.UserHandle
                 * */
                if (config.forAllUser) it.addAll("--user", "all")
                else it.addAll("--user", "${extra.userId}")
                /*
                 * @url http://aospxref.com/android-13.0.0_r3/xref/frameworks/base/services/core/java/com/android/server/pm/PackageManagerShellCommand.java#3982
                 * --dont-kill: installing a new feature split, don't kill running app
                 * 补充安装*/
                if (entities.count { it is AppEntity.MainEntity } == 0) it.add("--dont-kill")
            }
        }
        console.waitIt()
        if (console.exitValue() != 0) {
            console.inputStream.copyTo(System.out)
            console.errorStream.copyTo(System.err)
            return@withContext null
        }
        val input = console.inputStream.reader().readText()
        val matcher = Pattern.compile("(\\d+)").matcher(input)
        if (!matcher.find()) {
            System.out.writer().write(input)
            console.errorStream.copyTo(System.err)
            return@withContext null
        }
        val sessionId = matcher.group()
        return@withContext sessionId
    }

    private suspend fun installIts(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity,
        sessionId: String
    ): Boolean {
        for (entity in entities) {
            if (!installIt(config, entity, extra, sessionId)) return false
        }
        return true
    }

    private suspend fun installIt(
        config: ConfigEntity,
        entity: AppEntity,
        extra: InstallExtraEntity,
        sessionId: String
    ): Boolean {
        (when (entity) {
            is AppEntity.MainEntity -> entity.data.getInputStream()
            is AppEntity.SplitEntity -> entity.data.getInputStream()
        } ?: return false).use { inputStream ->
            val console = ConsoleRepoUtil.open {
                command = mutableListOf<String>().also {
                    it.addAll(
                        "pm",
                        "install-write",
                        "-S",
                        "${inputStream.available()}",
                        sessionId,
                        "${entity.hashCode()}.apk"
                    )
                }
            }
            console.waitIt()
            console.inputStream.readBytes().decodeToString().also { println(it) }
            console.errorStream.readBytes().decodeToString().also { println(it) }
            console.inputStream.copyTo(System.out)
            console.errorStream.copyTo(System.err)
            if (console.exitValue() != 0) {
                console.inputStream.copyTo(System.out)
                console.errorStream.copyTo(System.err)
                return false
            }
        }
        return true
    }

    private suspend fun commit(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity,
        sessionId: String
    ) = withContext(Dispatchers.IO) {
        val console = ConsoleRepoUtil.open {
            command = mutableListOf<String>().also {
                it.addAll(
                    "pm",
                    "install-commit",
                    sessionId,
                )
            }
        }
        console.waitIt()
        if (console.exitValue() != 0) {
            console.inputStream.copyTo(System.out)
            console.errorStream.copyTo(System.err)
        }
    }
}