package com.rosan.installer.data.recycle.model.impl

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.Keep
import com.rosan.installer.IPrivilegedService
import com.rosan.installer.IShizukuUserService
import com.rosan.installer.data.recycle.model.entity.DefaultPrivilegedService
import com.rosan.installer.data.recycle.repo.recyclable.UserService
import com.rosan.installer.data.recycle.repo.Recycler
import com.rosan.installer.data.recycle.util.requireShizukuPermissionGranted
import com.rosan.installer.di.init.processModules
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import rikka.shizuku.Shizuku
import kotlin.system.exitProcess

object ShizukuUserServiceRecycler : Recycler<ShizukuUserServiceRecycler.UserServiceProxy>(),
    KoinComponent {
    class UserServiceProxy(val service: IShizukuUserService) : UserService {
        override val privileged: IPrivilegedService = service.privilegedService

        override fun close() = service.destroy()
    }

    class ShizukuUserService @Keep constructor(context: Context) : IShizukuUserService.Stub() {
        init {
            startKoin {
                modules(processModules)
                androidContext(context)
            }
        }

        private val privileged = DefaultPrivilegedService()

        override fun destroy() {
            exitProcess(0)
        }

        override fun getPrivilegedService(): IPrivilegedService = privileged
    }

    private val context by inject<Context>()

    override fun onMake(): UserServiceProxy = runBlocking {
        requireShizukuPermissionGranted {
            onInnerMake()
        }
    }

    private suspend fun onInnerMake(): UserServiceProxy = callbackFlow {
        Shizuku.bindUserService(Shizuku.UserServiceArgs(
            ComponentName(
                context, ShizukuUserService::class.java
            )
        ).processNameSuffix("shizuku_privileged"), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                trySend(UserServiceProxy(IShizukuUserService.Stub.asInterface(service)))
                service?.linkToDeath({
                    if (entity?.service == service) recycleForcibly()
                }, 0)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                close()
            }
        })
        awaitClose { }
    }.first()
}