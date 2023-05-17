package com.rosan.installer.data.console.model.impl

import android.content.Context
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener
import com.rosan.installer.data.console.repo.ConsoleBuilderRepo
import com.rosan.installer.data.console.repo.ConsoleRepo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class DhizukuConsoleBuilderRepoImpl : ConsoleBuilderRepo(), KoinComponent {
    private val context by inject<Context>()

    private fun _open(): ConsoleRepoImpl {
        return ConsoleRepoImpl(
            Dhizuku.newProcess(
                command.toTypedArray(),
                environment?.toTypedArray(),
                directory?.let { File(it) }
            )
        )
    }

    override suspend fun open(): ConsoleRepo {
        return callbackFlow {
            Dhizuku.init(context)
            if (Dhizuku.isPermissionGranted()) {
                send(_open())
                awaitClose { }
            } else {
                Dhizuku.requestPermission(object : DhizukuRequestPermissionListener() {
                    override fun onRequestPermission(grantResult: Int) {
                        kotlin.runCatching { _open() }
                            .onSuccess {
                                trySend(it)
                            }
                            .onFailure {
                                close(it)
                            }
                    }
                })
                awaitClose {}
            }
        }.first()
    }
}