package com.rosan.installer.di.init

import com.rosan.installer.di.appModule
import com.rosan.installer.di.init.process.contextModule
import com.rosan.installer.di.protobufModule
import com.rosan.installer.di.reflectModule
import com.rosan.installer.di.resModule

val processModules = listOf(
    contextModule,
    protobufModule,
    appModule,
    resModule,
    reflectModule
)


