package com.rosan.installer.data.app.model.entity

import android.system.Os
import android.system.OsConstants
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

sealed class DataEntity(open var source: DataEntity? = null) {
    abstract fun getInputStream(): InputStream?

    fun getInputStreamWhileNotEmpty(): InputStream? = getInputStream() ?: source?.getInputStream()

    fun getSourceTop(): DataEntity = source?.getSourceTop() ?: this

    class FileEntity(val path: String) : DataEntity() {
        override fun getInputStream() = File(path).inputStream()

        override fun toString() = path
    }

    class ZipFileEntity(val path: String, val name: String) : DataEntity() {
        override fun getInputStream(): InputStream? = ZipFile(path).let {
            val entry = it.getEntry(name) ?: return@let null
            it.getInputStream(entry)
        }

        override fun toString() = "$path!$name"
    }

    class FileDescriptorEntity(val pid: Int, val descriptor: Int) : DataEntity() {
        fun getFileDescriptor(): FileDescriptor? {
            if (Os.getpid() != pid) return null
            val fileDescriptor = FileDescriptor()
            kotlin.runCatching { FileDescriptor::class.java.getDeclaredField("descriptor") }
                .onSuccess {
                    it.isAccessible = true
                    it.set(fileDescriptor, descriptor)
                }.onFailure {
                    it.printStackTrace()
                }
            if (!fileDescriptor.valid()) return null
            Os.lseek(fileDescriptor, 0, OsConstants.SEEK_SET)
            return fileDescriptor
        }

        override fun getInputStream(): InputStream {
            val fileDescriptor = getFileDescriptor()
            if (fileDescriptor != null) {
                return FileInputStream(fileDescriptor)
            }
            return File("/proc/$pid/fd/$descriptor").inputStream()
        }

        override fun toString() = "/proc/$pid/fd/$descriptor"
    }

    class ZipInputStreamEntity(val name: String, val parent: DataEntity) : DataEntity() {
        override fun getInputStream(): InputStream? {
            val inputStream = parent.getInputStream() ?: return null
            val zip = ZipInputStream(inputStream)
            var result: InputStream? = null
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.name != name) continue
                result = zip
                break
            }
            return result
        }
    }
}