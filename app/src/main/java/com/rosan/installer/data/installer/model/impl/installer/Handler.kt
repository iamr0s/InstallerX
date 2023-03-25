package com.rosan.installer.data.installer.model.impl.installer

import com.rosan.installer.data.installer.model.impl.InstallerRepoImpl

abstract class Handler(val worker: InstallerRepoImpl.MyWorker) {
    abstract suspend fun onStart()

    abstract suspend fun onFinish()
}