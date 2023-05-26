package com.rosan.installer.data.app.util

import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.app.model.entity.InstallEntity

fun List<InstallEntity>.sourcePath(): Array<String> = map {
    when (val data = it.data.getSourceTop()) {
        is DataEntity.FileEntity -> data.path
        is DataEntity.ZipFileEntity -> data.path
        else -> null
    }
}.filterNotNull().toTypedArray()
