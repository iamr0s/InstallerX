package com.rosan.installer.data.recycle.model.impl

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.Keep
import com.rosan.installer.IPrivilegedService
import com.rosan.installer.IShizukuUserService
import com.rosan.installer.data.recycle.model.entity.DefaultPrivilegedService
import com.rosan.installer.data.recycle.repo.Recycler
import com.rosan.installer.data.recycle.util.requireShizukuPermissionGranted
import com.rosan.installer.di.init.processModules
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import rikka.shizuku.Shizuku
import java.io.Closeable
import kotlin.system.exitProcess

object ShizukuPrivilegedServiceRecycler : Recycler<ShizukuPrivilegedServiceRecycler.Privileged>(),
    KoinComponent {
    class Privileged(private val service: IShizukuUserService) : IPrivilegedService, Closeable {
        private val privileged = service.privilegedService

        override fun asBinder(): IBinder = privileged.asBinder()

        override fun delete(paths: Array<String>?) = privileged.delete(paths)

        override fun setDefaultInstaller(component: ComponentName?, enable: Boolean) =
            privileged.setDefaultInstaller(component, enable)

        override fun close() = service.destroy()
    }

    class ShizukuUserService @Keep constructor() : IShizukuUserService.Stub() {
        init {
            startKoin {
                modules(processModules)
            }
        }

        private val privileged = DefaultPrivilegedService()

        override fun destroy() {
            exitProcess(0)
        }

        override fun getPrivilegedService(): IPrivilegedService = privileged
    }

    private val context by inject<Context>()

    override fun onMake(): Privileged = runBlocking {
        requireShizukuPermissionGranted {
            onInnerMake()
        }
    }

    private suspend fun onInnerMake(): Privileged = callbackFlow {
        Shizuku.bindUserService(Shizuku.UserServiceArgs(
            ComponentName(
                context, ShizukuUserService::class.java
            )
        ).processNameSuffix("shizuku_privileged"), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                trySend(Privileged(IShizukuUserService.Stub.asInterface(service)))
                service?.linkToDeath({
                    if (entity?.asBinder() == service) recycleForcibly()
                }, 0)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                close()
            }
        })
        awaitClose { }
    }.first()
}