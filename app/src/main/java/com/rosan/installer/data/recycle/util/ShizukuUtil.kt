package com.rosan.installer.data.recycle.util

import android.content.pm.PackageManager
import com.rosan.installer.data.recycle.model.exception.ShizukuNotWorkException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import rikka.shizuku.Shizuku

suspend fun <T> requireShizukuPermissionGranted(action: suspend () -> T): T {
    callbackFlow {
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            send(Unit)
            awaitClose()
        } else {
            val requestCode = (Int.MIN_VALUE..Int.MAX_VALUE).random()
            val listener =
                Shizuku.OnRequestPermissionResultListener { _requestCode, grantResult ->
                    if (_requestCode != requestCode) return@OnRequestPermissionResultListener
                    if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
                        trySend(Unit)
                    else close(Exception("sui/shizuku permission denied"))
                }
            Shizuku.addRequestPermissionResultListener(listener)
            Shizuku.requestPermission(requestCode)
            awaitClose { Shizuku.removeRequestPermissionResultListener(listener) }
        }
    }.catch {
        throw ShizukuNotWorkException(it)
    }.first()
    return action()
}