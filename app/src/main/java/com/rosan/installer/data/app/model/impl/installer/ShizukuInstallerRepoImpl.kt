package com.rosan.installer.data.app.model.impl.installer

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import com.rosan.installer.IShizukuUserService
import com.rosan.installer.data.app.model.entity.InstallEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.model.impl.privileged.ShizukuUserService
import com.rosan.installer.data.console.model.exception.ShizukuNotWorkException
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper

object ShizukuInstallerRepoImpl : IBinderInstallerRepoImpl(), KoinComponent {
    private val context by inject<Context>()

    private val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(
            context,
            ShizukuUserService::class.java
        )
    ).processNameSuffix("shizuku_privileged")

    private var connection: ServiceConnection? = null

    private var service: IShizukuUserService? = null

    override suspend fun iBinderWrapper(iBinder: IBinder): IBinder {
        return callbackFlow<IBinder> {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                kotlin.runCatching { ShizukuBinderWrapper(iBinder) }
                    .onFailure { close(it) }
                    .onSuccess { send(it) }
                awaitClose { }
            } else {
                val requestCode = (Int.MIN_VALUE..Int.MAX_VALUE).random()
                val listener =
                    Shizuku.OnRequestPermissionResultListener { _requestCode, grantResult ->
                        if (_requestCode != requestCode) return@OnRequestPermissionResultListener
                        kotlin.runCatching {
                            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
                                ShizukuBinderWrapper(iBinder)
                            else throw Exception("sui/shizuku permission denied")
                        }.onFailure { close(it) }.onSuccess { trySend(it) }
                    }
                Shizuku.addRequestPermissionResultListener(listener)
                Shizuku.requestPermission(requestCode)
                awaitClose {
                    Shizuku.removeRequestPermissionResultListener(listener)
                }
            }
        }.catch {
            throw ShizukuNotWorkException(it)
        }.first()
    }

    override suspend fun doDeleteWork(path: String) {
        if (service == null) service = callbackFlow<IShizukuUserService> {
            val localConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    trySend(IShizukuUserService.Stub.asInterface(service))
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    close()
                }
            }
            Shizuku.bindUserService(
                userServiceArgs, localConnection
            )
            connection = localConnection
            awaitClose {}
        }.first()
        service?.privilegedService?.deletePath(path)
    }

    override suspend fun doFinishWork(
        config: ConfigEntity,
        entities: List<InstallEntity>,
        extra: InstallExtraEntity
    ) {
        super.doFinishWork(config, entities, extra)
        connection?.let { Shizuku.unbindUserService(userServiceArgs, it, false) }
    }
}