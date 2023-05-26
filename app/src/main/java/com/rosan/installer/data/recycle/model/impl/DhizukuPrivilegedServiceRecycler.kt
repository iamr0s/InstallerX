package com.rosan.installer.data.recycle.model.impl

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import androidx.annotation.Keep
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuUserServiceArgs
import com.rosan.installer.IDhizukuUserService
import com.rosan.installer.IPrivilegedService
import com.rosan.installer.data.recycle.model.entity.DhizukuPrivilegedService
import com.rosan.installer.data.recycle.repo.Recycler
import com.rosan.installer.data.recycle.util.requireDhizukuPermissionGranted
import com.rosan.installer.di.init.processModules
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import java.io.Closeable

object DhizukuPrivilegedServiceRecycler : Recycler<DhizukuPrivilegedServiceRecycler.Privileged>(),
    KoinComponent {
    class Privileged(
        private val connection: ServiceConnection,
        service: IDhizukuUserService
    ) : IPrivilegedService, Closeable {
        private val privileged = service.privilegedService

        override fun asBinder(): IBinder = privileged.asBinder()

        override fun delete(paths: Array<String>?) = privileged.delete(paths)

        override fun setDefaultInstaller(component: ComponentName?, enable: Boolean) =
            privileged.setDefaultInstaller(component, enable)

        override fun close() {
            Dhizuku.unbindUserService(connection)
        }
    }

    class DhizukuUserService @Keep constructor(context: Context) : IDhizukuUserService.Stub() {
        init {
            startKoin {
                modules(processModules)
            }
        }

        private val privileged = DhizukuPrivilegedService(context)

        override fun getPrivilegedService(): IPrivilegedService = privileged
    }

    private val context by inject<Context>()

    override fun onMake(): Privileged = runBlocking {
        requireDhizukuPermissionGranted {
            onInnerMake()
        }
    }

    private suspend fun onInnerMake(): Privileged = callbackFlow {
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                trySend(Privileged(this, IDhizukuUserService.Stub.asInterface(service)))
                service?.linkToDeath({
                    if (entity?.asBinder() == service) recycleForcibly()
                }, 0)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                close()
            }
        }
        Dhizuku.bindUserService(
            DhizukuUserServiceArgs(
                ComponentName(
                    context, DhizukuUserService::class.java
                )
            ), connection
        )
        awaitClose { }
    }.first()
}