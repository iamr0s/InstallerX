package com.rosan.installer.data.app.model.impl.installer

import android.content.*
import android.content.pm.IPackageInstaller
import android.content.pm.IPackageInstallerSession
import android.content.pm.IPackageManager
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.Session
import android.os.*
import com.rosan.dhizuku.shared.DhizukuVariables
import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.repo.InstallerRepo
import com.rosan.installer.data.app.util.InstallFlag
import com.rosan.installer.data.app.util.PackageInstallerUtil.Companion.installFlags
import com.rosan.installer.data.app.util.PackageManagerUtil
import com.rosan.installer.data.reflect.repo.ReflectRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import okhttp3.internal.closeQuietly
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import java.lang.reflect.Field
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

abstract class IBinderInstallerRepoImpl : InstallerRepo, KoinComponent {
    protected val reflect = get<ReflectRepo>()

    protected abstract suspend fun iBinderWrapper(iBinder: IBinder): IBinder

    private suspend fun getFiled(any: Class<*>, name: String, clazz: Class<*>): Field? {
        var field = reflect.getDeclaredField(any, name)
        field?.isAccessible = true
        if (field?.type != clazz) {
            val fields = reflect.getDeclaredFields(any)
            for (item in fields) {
                if (item.type != clazz) continue
                field = item
                break
            }
        }
        field?.isAccessible = true
        return field
    }

    private suspend fun getPackageInstaller(
        config: ConfigEntity, entities: List<InstallEntity>, extra: InstallExtraEntity
    ): PackageInstaller {
        val iPackageManager =
            IPackageManager.Stub.asInterface(iBinderWrapper(ServiceManager.getService("package")))
        val iPackageInstaller =
            IPackageInstaller.Stub.asInterface(iBinderWrapper(iPackageManager.packageInstaller.asBinder()))

        var installerPackageName = config.installer
        if (config.authorizer == ConfigEntity.Authorizer.Dhizuku)
            installerPackageName = DhizukuVariables.PACKAGE_NAME

        return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            reflect.getDeclaredConstructor(
                PackageInstaller::class.java,
                IPackageInstaller::class.java,
                String::class.java,
                String::class.java,
                Int::class.java,
            )!!.also {
                it.isAccessible = true
            }.newInstance(iPackageInstaller, installerPackageName, null, extra.userId)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            reflect.getDeclaredConstructor(
                PackageInstaller::class.java,
                IPackageInstaller::class.java,
                String::class.java,
                Int::class.java,
            )!!.also {
                it.isAccessible = true
            }.newInstance(iPackageInstaller, installerPackageName, extra.userId)
        } else {
            reflect.getDeclaredConstructor(
                PackageInstaller::class.java,
                Context::class.java,
                PackageInstaller::class.java,
                IPackageInstaller::class.java,
                String::class.java,
                Int::class.java
            )!!.also {
                it.isAccessible = true
            }.newInstance(null, null, iPackageInstaller, installerPackageName, extra.userId)
        }) as PackageInstaller
    }

    private suspend fun setSessionIBinder(session: Session) {
        val field = getFiled(session::class.java, "mSession", IPackageInstallerSession::class.java)
            ?: return
        val iBinder = (field.get(session) as IInterface).asBinder()
        field.set(
            session, IPackageInstallerSession.Stub.asInterface(iBinderWrapper(iBinder))
        )
    }

    override suspend fun doWork(
        config: ConfigEntity, entities: List<InstallEntity>, extra: InstallExtraEntity
    ) {
        entities.groupBy { it.packageName }.forEach { (packageName, entities) ->
            doInnerWork(config, entities, extra, packageName)
        }
        doFinishWork(config, entities, extra)
    }

    private suspend fun doInnerWork(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity,
        packageName: String
    ) {
        if (entities.isEmpty()) return
        var session: Session? = null
        try {
            session = createSession(config, entities, extra, packageName)
            installIts(config, entities, extra, session)
            commit(config, entities, extra, session)
        } finally {
            session?.closeQuietly()
        }
    }

    private suspend fun createSession(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity,
        packageName: String
    ): Session {
        val packageInstaller = getPackageInstaller(config, entities, extra)
        val params = PackageInstaller.SessionParams(
            when (entities.count { it.name == "base.apk" }) {
                1 -> PackageInstaller.SessionParams.MODE_FULL_INSTALL
                0 -> PackageInstaller.SessionParams.MODE_INHERIT_EXISTING
                else -> throw Exception("can't install multiple package name in single session")
            }
        )
        params.setAppPackageName(packageName)
        params.installFlags = params.installFlags or InstallFlag.INSTALL_REPLACE_EXISTING.value

        if (config.allowTestOnly) params.installFlags =
            params.installFlags or InstallFlag.INSTALL_ALLOW_TEST.value

        if (config.allowDowngrade) {
            params.installFlags = params.installFlags or InstallFlag.INSTALL_REQUEST_DOWNGRADE.value
            params.installFlags = params.installFlags or InstallFlag.INSTALL_ALLOW_DOWNGRADE.value
        }

        if (config.forAllUser) params.installFlags =
            params.installFlags or InstallFlag.INSTALL_ALL_USERS.value
        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)
        setSessionIBinder(session)
        return session
    }

    private suspend fun installIts(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity,
        session: Session
    ) {
        for (entity in entities) installIt(config, entity, extra, session)
    }

    private suspend fun installIt(
        config: ConfigEntity, entity: InstallEntity, extra: InstallExtraEntity, session: Session
    ) {
        val inputStream = entity.data.getInputStreamWhileNotEmpty()
            ?: throw Exception("can't open input stream for this data: '${entity.data}'")
        session.openWrite(entity.name, 0, inputStream.available().toUInt().toLong()).use {
            inputStream.copyTo(it)
            session.fsync(it)
        }
    }

    private suspend fun commit(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity,
        session: Session
    ) {
        val receiver = LocalIntentReceiver()
        session.commit(receiver.getIntentSender())
        val result = receiver.getResult()
        PackageManagerUtil.installResultVerify(result)
    }

    private suspend fun doFinishWork(
        config: ConfigEntity, entities: List<InstallEntity>, extra: InstallExtraEntity
    ) {
        if (!config.autoDelete) return
        entities.forEach {
            val path = when (val data = it.data.getSourceTop()) {
                is DataEntity.FileEntity -> data.path
                is DataEntity.ZipFileEntity -> data.path
                else -> null
            }
            if (path != null) File(path).delete()
        }
    }

    class LocalIntentReceiver : KoinComponent {
        private val reflect = get<ReflectRepo>()

        private val result = LinkedBlockingQueue<Intent>()

        private val localSender = object : IIntentSender.Stub() {
            // this api only work for upper Android O (8.0)
            // see this url:
            // Android N (7.1): http://aospxref.com/android-7.1.2_r39/xref/frameworks/base/core/java/android/content/IIntentSender.aidl
            // Android O (8.0): http://aospxref.com/android-8.0.0_r36/xref/frameworks/base/core/java/android/content/IIntentSender.aidl
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

            fun send(
                code: Int,
                intent: Intent?,
                resolvedType: String?,
                finishedReceiver: IIntentReceiver?,
                requiredPermission: String?,
                options: Bundle?
            ) {
                send(
                    code, intent, resolvedType, null, finishedReceiver, requiredPermission, options
                )
            }

            override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return super.onTransact(
                    code, data, reply, flags
                )
                val descriptor = "android.content.IIntentSender"
                return when (code) {
                    1 -> {
                        data.enforceInterface(descriptor)
                        send(
                            data.readInt(),
                            if (data.readInt() != 0) Intent.CREATOR.createFromParcel(data) else null,
                            data.readString(),
                            IIntentReceiver.Stub.asInterface(data.readStrongBinder()),
                            data.readString(),
                            if (data.readInt() != 0) Bundle.CREATOR.createFromParcel(data) else null
                        )
                        true
                    }
                    0x5F4E5446 -> {
                        reply?.writeString(descriptor)
                        true
                    }
                    else -> return super.onTransact(code, data, reply, flags)
                }
            }
        }

        fun getIntentSender(): IntentSender {
            return reflect.getDeclaredConstructor(
                IntentSender::class.java, IIntentSender::class.java
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