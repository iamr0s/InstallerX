package com.rosan.installer.data.app.model.impl.installer

import android.os.IBinder
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.installer.data.recycle.util.requireDhizukuPermissionGranted
import org.koin.core.component.KoinComponent

object DhizukuInstallerRepoImpl : IBinderInstallerRepoImpl(), KoinComponent {
    override suspend fun iBinderWrapper(iBinder: IBinder): IBinder =
        requireDhizukuPermissionGranted {
            Dhizuku.binderWrapper(iBinder)
        }
}