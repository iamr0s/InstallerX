package com.rosan.installer.data.app.model.impl.installer

import android.content.Context
import android.content.IIntentReceiver
import android.content.IIntentSender
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.os.Bundle
import android.os.IBinder
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.repo.InstallerRepo
import com.rosan.installer.data.app.util.InstallFlag
import com.rosan.installer.data.app.util.PackageInstallerUtil.Companion.installFlags
import com.rosan.installer.data.common.model.entity.ErrorEntity
import com.rosan.installer.data.reflect.repo.ReflectRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.io.File
import java.io.InputStream
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class ProcessInstallerRepoImpl : InstallerRepo, KoinComponent {
    //    private fun getPackageManager(
//        config: ConfigEntity,
//        entities: List<AppEntity>,
//        extra: InstallExtraEntity
//    ): PackageInstaller {
//        val iPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"))
//        val iPackageInstaller = iPackageManager.packageInstaller
//        val repo = get<ReflectRepo>()
//        return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            repo.getDeclaredConstructor(
//                PackageInstaller::class.java,
//                IPackageInstaller::class.java,
//                String::class.java,
//                String::class.java,
//                Int::class.java,
//            )!!.also {
//                it.isAccessible = true
//            }.newInstance(iPackageInstaller, config.installer, null, extra.userId)
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            repo.getDeclaredConstructor(
//                PackageInstaller::class.java,
//                IPackageInstaller::class.java,
//                String::class.java,
//                Int::class.java,
//            )!!.also {
//                it.isAccessible = true
//            }.newInstance(iPackageInstaller, config.installer, extra.userId)
//        } else {
//            repo.getDeclaredConstructor(
//                PackageInstaller::class.java,
//                Context::class.java,
//                PackageManager::class.java,
//                IPackageInstaller::class.java,
//                String::class.java,
//                Int::class.java
//            )!!.also {
//                it.isAccessible = true
//            }.newInstance(null, null, iPackageInstaller, config.installer, extra.userId)
//        }) as PackageInstaller
//    }
    private val context by inject<Context>()

    private val packageManager = context.packageManager

    private fun getPackageInstaller(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity
    ): PackageInstaller {
        val packageInstaller = packageManager.packageInstaller
        val reflect: ReflectRepo = get()
        reflect.getDeclaredField(
            packageInstaller::class.java,
            "mInstallerPackageName"
        )?.let {
            it.isAccessible = true
            it.set(packageInstaller, config.installer)
        }
        reflect.getDeclaredField(
            packageInstaller::class.java,
            "mUserId"
        )?.let {
            it.isAccessible = true
            it.set(packageInstaller, extra.userId)
        }
        return packageInstaller
    }

    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity
    ) = withContext(Dispatchers.IO) {
        entities.groupBy { it.packageName }.forEach { (packageName, entities) ->
            doInnerWork(config, packageName, entities, extra)
        }
        doFinishWork(config, entities, extra)
    }

    private suspend fun doInnerWork(
        config: ConfigEntity,
        packageName: String,
        entities: List<AppEntity>,
        extra: InstallExtraEntity
    ) = withContext(Dispatchers.IO) {
        if (entities.isEmpty()) return@withContext
        val packageInstaller = getPackageInstaller(config, entities, extra)
        val session = createSession(config, packageName, entities, extra, packageInstaller)
        installIts(config, entities, extra, session)
        commit(config, entities, extra, session)
    }

    private suspend fun createSession(
        config: ConfigEntity,
        packageName: String,
        entities: List<AppEntity>,
        extra: InstallExtraEntity,
        packageInstaller: PackageInstaller
    ): PackageInstaller.Session = withContext(Dispatchers.IO) {
        // MODE_FULL_INSTALL: 覆盖安装
        // MODE_INHERIT_EXISTING: 补充安装
        // see https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/content/pm/PackageInstaller.java#1670
        val params =
            PackageInstaller.SessionParams(
                when (entities.count { it is AppEntity.MainEntity }) {
                    1 -> PackageInstaller.SessionParams.MODE_FULL_INSTALL
                    0 -> PackageInstaller.SessionParams.MODE_INHERIT_EXISTING
                    else -> throw ErrorEntity("more than one base apk")
                }
            )
        params.setAppPackageName(packageName)

        params.installFlags =
            params.installFlags or InstallFlag.INSTALL_REPLACE_EXISTING.value

        if (config.allowTestOnly) params.installFlags =
            params.installFlags or InstallFlag.INSTALL_ALLOW_TEST.value

        if (config.allowDowngrade) {
            params.installFlags =
                params.installFlags or InstallFlag.INSTALL_REQUEST_DOWNGRADE.value
            params.installFlags =
                params.installFlags or InstallFlag.INSTALL_ALLOW_DOWNGRADE.value
        }

        if (config.forAllUser) params.installFlags =
            params.installFlags or InstallFlag.INSTALL_ALL_USERS.value
        val sessionId = packageInstaller.createSession(params)
        return@withContext packageInstaller.openSession(sessionId)
    }

    private suspend fun installIts(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity,
        session: PackageInstaller.Session
    ) {
        for (entity in entities) {
            installIt(config, entity, extra, session)
        }
    }

    private suspend fun installIt(
        config: ConfigEntity,
        entity: AppEntity,
        extra: InstallExtraEntity,
        session: PackageInstaller.Session
    ) {
        when (entity) {
            is AppEntity.MainEntity -> {
                val inputStream =
                    entity.data.getInputStream()
                        ?: throw ErrorEntity("can not open input steam")
                writeEntity(session, "base.apk", inputStream)
            }
            is AppEntity.SplitEntity -> {
                val inputStream =
                    entity.data.getInputStream()
                        ?: throw ErrorEntity("can not open input steam")
                writeEntity(session, "${entity.splitName}.apk", inputStream)
            }
        }
    }

    private suspend fun writeEntity(
        session: PackageInstaller.Session,
        name: String,
        inputStream: InputStream
    ) = withContext(Dispatchers.IO) {
        session.openWrite(name, 0, inputStream.available().toUInt().toLong()).use { out ->
            inputStream.copyTo(out)
            session.fsync(out)
        }
    }

    private suspend fun commit(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity,
        session: PackageInstaller.Session,
    ) = withContext(Dispatchers.IO) {
        val receiver = LocalIntentReceiver()
        session.commit(receiver.getIntentSender())
        val result = receiver.getResult()
        val status =
            result.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        if (status != PackageInstaller.STATUS_SUCCESS)
            throw ErrorEntity(
                "Failure [${
                    result.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                }]"
            )
    }

    private fun doFinishWork(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity
    ) {
        if (!config.autoDelete) return
        entities.forEach {
            val data = when (it) {
                is AppEntity.MainEntity -> it.data
                is AppEntity.SplitEntity -> it.data
            }
            val path = when (data) {
                is DataEntity.FileEntity -> data.path
                is DataEntity.ZipEntity -> data.path
            }
            File(path).delete()
        }
    }

    class LocalIntentReceiver : KoinComponent {
        private val result = LinkedBlockingQueue<Intent>()

        private val localSender = object : IIntentSender.Stub() {
            override fun send(
                code: Int,
                intent: Intent?,
                resolvedType: String?,
                whitelistToken: IBinder?,
                finishedReceiver: IIntentReceiver?,
                requiredPermission: String?,
                options: Bundle?
            ) {
                result.offer(intent, 5, TimeUnit.SECONDS)
            }
        }

        fun getIntentSender(): IntentSender {
            return get<ReflectRepo>().getDeclaredConstructor(
                IntentSender::class.java,
                IIntentSender::class.java
            )!!.also {
                it.isAccessible = true
            }.newInstance(localSender) as IntentSender
        }

        fun getResult(): Intent {
            return try {
                result.take()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }
        }
    }
}