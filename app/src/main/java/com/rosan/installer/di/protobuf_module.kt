package com.rosan.installer.di

import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.DataEntity
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.dsl.module

@OptIn(ExperimentalSerializationApi::class)
val protobufModule = module {
    single {
        SerializersModule {
            polymorphic(DataEntity::class) {
                subclass(DataEntity.FileEntity::class)
                subclass(DataEntity.ZipEntity::class)
            }
            polymorphic(AppEntity::class) {
                subclass(AppEntity.MainEntity::class)
                subclass(AppEntity.SplitEntity::class)
            }
        }
    }

    single {
        ProtoBuf {
            serializersModule = get()
        }
    }

    single {
        Json {
            serializersModule = get()
            ignoreUnknownKeys = true
        }
    }
}