package com.rosan.installer.data.installer.model.impl.installer

import com.rosan.installer.data.installer.model.entity.ProgressEntity
import com.rosan.installer.data.installer.model.impl.InstallerRepoImpl
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ProgressHandler(
    worker: InstallerRepoImpl.MyWorker
) : Handler(worker) {
    private var job: Job? = null

    override suspend fun onStart() {
        job = worker.scope.launch {
            worker.impl.progress.collect {
                when (it) {
                    is ProgressEntity.ResolvedFailed -> onResolvedFailed()
                    is ProgressEntity.ResolveSuccess -> onResolveSuccess()
                    is ProgressEntity.AnalysedSuccess -> onAnalysedSuccess()
                    else -> {}
                }
            }
        }
    }

    override suspend fun onFinish() {
        job?.cancel()
    }

    private suspend fun onResolvedFailed() {
        onResolved(false)
    }

    private suspend fun onResolveSuccess() {
        onResolved(true)
    }

    private fun onResolved(success: Boolean) {
        val installMode = worker.impl.config.installMode
        if (
            installMode == ConfigEntity.InstallMode.Notification
            || installMode == ConfigEntity.InstallMode.AutoNotification
        ) {
            worker.impl.background(true)
        }
        if (success) {
            worker.impl.analyse()
        }
    }

    private fun onAnalysedSuccess() {
        val installMode = worker.impl.config.installMode
        if (
            installMode != ConfigEntity.InstallMode.AutoDialog
            && installMode != ConfigEntity.InstallMode.AutoNotification
        ) return
        if (worker.impl.entities.count { it.selected } != 1) return
        worker.impl.install()
    }
}