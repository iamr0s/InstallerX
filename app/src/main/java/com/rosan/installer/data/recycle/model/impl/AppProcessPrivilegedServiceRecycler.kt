package com.rosan.installer.data.recycle.model.impl

import android.content.ComponentName
import android.content.Context
import android.os.IBinder
import androidx.annotation.Keep
import com.rosan.app_process.AppProcess
import com.rosan.installer.IAppProcessService
import com.rosan.installer.IPrivilegedService
import com.rosan.installer.data.recycle.model.entity.DefaultPrivilegedService
import com.rosan.installer.data.recycle.repo.Recycler
import com.rosan.installer.di.init.processModules
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import java.io.Closeable
import kotlin.system.exitProcess

class AppProcessPrivilegedServiceRecycler(private val shell: String) :
    Recycler<AppProcessPrivilegedServiceRecycler.Privileged>(), KoinComponent {
    class Privileged(private val service: IAppProcessService) : IPrivilegedService, Closeable {
        private val privileged = service.privilegedService

        override fun asBinder(): IBinder = privileged.asBinder()

        override fun delete(paths: Array<String>?) = privileged.delete(paths)

        override fun setDefaultInstaller(component: ComponentName?, enable: Boolean) =
            privileged.setDefaultInstaller(component, enable)

        override fun close() = service.quit()
    }

    class AppProcessService @Keep constructor() : IAppProcessService.Stub() {
        init {
            startKoin {
                modules(processModules)
            }
        }

        private val privileged = DefaultPrivilegedService()

        override fun quit() {
            exitProcess(0)
        }

        override fun getPrivilegedService(): IPrivilegedService = privileged
    }

    private val context by inject<Context>()

    private lateinit var appProcessRecycler: Recycler<AppProcess>

    override fun onMake(): Privileged {
        appProcessRecycler = AppProcessRecyclers.get(shell)
        val binder = appProcessRecycler.make().entity.startProcess(
            ComponentName(
                context, AppProcessService::class.java
            )
        )
        binder.linkToDeath({
            if (entity?.asBinder() == binder) recycleForcibly()
        }, 0)
        return Privileged(IAppProcessService.Stub.asInterface(binder))
    }

    override fun onRecycle() {
        super.onRecycle()
        appProcessRecycler.recycle()
    }
}