package com.rosan.installer.di

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.dsl.module

@OptIn(ExperimentalSerializationApi::class)
val protobufModule = module {
    single {
        SerializersModule {
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