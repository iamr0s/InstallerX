package com.rosan.installer.data.process.model.impl

import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.model.impl.installer.ProcessInstallerRepoImpl
import com.rosan.installer.data.process.repo.ProcessRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromHexString
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class InstallerProcessRepoImpl {
    companion object : ProcessRepo(), KoinComponent {
        @JvmStatic
        override fun main(args: Array<String>) {
            super.main(args)
        }

        @OptIn(ExperimentalSerializationApi::class)
        override suspend fun onCreate(args: Array<String>) = withContext(Dispatchers.IO) {
            val serializer: ProtoBuf = get()
            val list = mutableListOf<String>()
            args.forEach {
                val len = it.toInt()
                list.add(stdinBytes(len + 1).decodeToString(0, len))
            }
            val config = serializer.decodeFromHexString<ConfigEntity>(list[0])
            val entities = serializer.decodeFromHexString<List<AppEntity>>(list[1])
            val extra = serializer.decodeFromHexString<InstallExtraEntity>(list[2])
            ProcessInstallerRepoImpl().doWork(config, entities, extra)
        }
    }
}
