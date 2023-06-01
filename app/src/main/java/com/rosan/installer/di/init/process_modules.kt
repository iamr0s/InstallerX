package com.rosan.installer.di.init

import com.rosan.installer.di.appModule
import com.rosan.installer.di.reflectModule
import com.rosan.installer.di.resModule
import com.rosan.installer.di.serializationModule

val processModules = listOf(
    serializationModule,
    appModule,
    resModule,
    reflectModule
)


