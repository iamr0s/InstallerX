package com.rosan.installer.data.app.model.entity

import kotlinx.serialization.Serializable
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

@Serializable
sealed class DataEntity {
    abstract fun getInputStream(): InputStream?

    @Serializable
    data class FileEntity(
        val path: String
    ) : DataEntity() {
        override fun getInputStream(): InputStream? = File(path).inputStream()

        override fun toString() = path
    }

    @Serializable
    data class ZipEntity(
        val path: String,
        val name: String
    ) : DataEntity() {
        override fun getInputStream(): InputStream? = ZipFile(path).let {
            val entry = it.getEntry(name) ?: return@let null
            it.getInputStream(entry)
        }

        override fun toString() = "$path?name=$name"
    }
}