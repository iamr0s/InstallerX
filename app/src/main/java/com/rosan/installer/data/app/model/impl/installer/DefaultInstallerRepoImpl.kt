package com.rosan.installer.data.app.model.impl.installer

import android.os.IBinder
import java.io.File

object DefaultInstallerRepoImpl : IBinderInstallerRepoImpl() {
    override suspend fun iBinderWrapper(iBinder: IBinder): IBinder = iBinder

    override suspend fun doDeleteWork(path: String) {
        val file = File(path)
        if (file.exists() && file.canWrite()) file.delete()
    }
}