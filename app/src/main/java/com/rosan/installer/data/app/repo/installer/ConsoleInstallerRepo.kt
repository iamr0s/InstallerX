package com.rosan.installer.data.app.repo.installer

import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.model.entity.error.ConsoleError
import com.rosan.installer.data.app.repo.InstallerRepo
import com.rosan.installer.data.app.repo.util.ConsoleUtil
import com.rosan.installer.data.common.model.entity.ErrorEntity
import com.rosan.installer.data.console.repo.ConsoleRepo
import com.rosan.installer.data.process.model.impl.InstallerProcessRepoImpl
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

interface ConsoleInstallerRepo : InstallerRepo, KoinComponent {
    suspend fun loadConsole(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity
    ): ConsoleRepo

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity
    ) = loadConsole(config, entities, extra).use {
        val util = ConsoleUtil(it)
        val scope = CoroutineScope(Dispatchers.IO)

        val inputJob = scope.async { util.inputBytes() }
        val errorJob = scope.async { util.errorBytes() }

        val serializer: ProtoBuf = get()
        val configHex = serializer.encodeToHexString(config)
        val entitiesHex = serializer.encodeToHexString(entities)
        val extraHex = serializer.encodeToHexString(extra)
        util.appendLine(
            ProcessRepo.request(
                InstallerProcessRepoImpl::class,
                configHex.length,
                entitiesHex.length,
                extraHex.length
            )
        )
        util.appendLine(configHex)
        util.appendLine(entitiesHex)
        util.appendLine(extraHex)
        util.appendLine("exit \$?")

        val inputBytes = inputJob.await()
        val errorBytes = errorJob.await()
        val code = it.exitValue()
        if (code == 0) return@use
        throw runCatching {
            serializer.decodeFromByteArray<ErrorEntity>(inputBytes)
        }.getOrNull()
            ?: ConsoleError(
                code = code,
                read = inputBytes.decodeToString(),
                error = errorBytes.decodeToString()
            )
    }
}