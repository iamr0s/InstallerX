package com.rosan.installer.data.app.model.impl.installer

import android.content.pm.PackageManager
import android.os.IBinder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper

class ShizukuInstallerRepoImpl : IBinderInstallerRepoImpl() {
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
                        kotlin.runCatching { ShizukuBinderWrapper(iBinder) }
                            .onFailure { close(it) }
                            .onSuccess { trySend(it) }
                    }
                Shizuku.addRequestPermissionResultListener(listener)
                Shizuku.requestPermission(requestCode)
                awaitClose {
                    Shizuku.removeRequestPermissionResultListener(listener)
                }
            }
        }.first()
    }
}