package com.rosan.installer.data.app.model.impl.installer

import android.os.IBinder
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent

object DhizukuInstallerRepoImpl : IBinderInstallerRepoImpl(), KoinComponent {
    override suspend fun iBinderWrapper(iBinder: IBinder): IBinder {
        return callbackFlow<IBinder> {
            if (Dhizuku.isPermissionGranted()) {
                kotlin.runCatching { Dhizuku.binderWrapper(iBinder) }
                    .onFailure { close(it) }
                    .onSuccess { send(it) }
                awaitClose { }
            } else {
                Dhizuku.requestPermission(object : DhizukuRequestPermissionListener() {
                    override fun onRequestPermission(grantResult: Int) {
                        kotlin.runCatching { Dhizuku.binderWrapper(iBinder) }
                            .onFailure { close(it) }
                            .onSuccess { trySend(it) }
                    }
                })
                awaitClose {}
            }
        }.first()
    }
}