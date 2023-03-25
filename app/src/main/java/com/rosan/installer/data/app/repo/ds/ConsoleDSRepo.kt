package com.rosan.installer.data.app.repo.ds

import android.util.Log
import com.rosan.installer.data.app.model.entity.error.ConsoleError
import com.rosan.installer.data.app.repo.DSRepo
import com.rosan.installer.data.app.repo.util.ConsoleUtil
import com.rosan.installer.data.common.model.entity.ErrorEntity
import com.rosan.installer.data.console.repo.ConsoleRepo
import com.rosan.installer.data.process.model.impl.DSProcessRepoImpl
import com.rosan.installer.data.process.repo.ProcessRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface ConsoleDSRepo : DSRepo, KoinComponent {
    suspend fun loadConsole(
        config: ConfigEntity, packageName: String, className: String, enabled: Boolean
    ): ConsoleRepo

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun doWork(
        config: ConfigEntity, packageName: String, className: String, enabled: Boolean
    ) = loadConsole(config, packageName, className, enabled).use {
        val util = ConsoleUtil(it)
        val scope = CoroutineScope(Dispatchers.IO)

        val inputJob = scope.async { util.inputBytes() }
        val errorJob = scope.async { util.errorBytes() }

        val serializer: ProtoBuf = get()
        val configHex = serializer.encodeToHexString(config)
        val packageNameHex = serializer.encodeToHexString(packageName)
        val classNameHex = serializer.encodeToHexString(className)
        val enabledHex = serializer.encodeToHexString(enabled)

        util.appendLine(
            ProcessRepo.request(
                DSProcessRepoImpl::class,
                configHex.length,
                packageNameHex.length,
                classNameHex.length,
                enabledHex.length
            )
        )
        util.appendLine(configHex)
        util.appendLine(packageNameHex)
        util.appendLine(classNameHex)
        util.appendLine(enabledHex)
        util.appendLine("exit \$?")

        val inputBytes = inputJob.await()
        val errorBytes = errorJob.await()
        val code = it.exitValue()
        Log.e("r0s", "${code}")
        Log.e("r0s", "${inputBytes.decodeToString()}")
        Log.e("r0s", "${errorBytes.decodeToString()}")
        throw runCatching {
            if (code == 0) return@use
            serializer.decodeFromByteArray<ErrorEntity>(inputBytes)
        }.getOrNull()
            ?: ConsoleError(
                code = code,
                read = inputBytes.decodeToString(),
                error = errorBytes.decodeToString()
            )
    }
}