package com.rosan.installer.data.log.model.impl

import android.annotation.SuppressLint
import android.content.Context
import com.rosan.installer.BuildConfig
import com.rosan.installer.build.RsConfig
import com.rosan.installer.data.log.repo.LogRepo
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileLogRepoImpl : LogRepo, KoinComponent {
    companion object {
        fun getWorkPath(context: Context): String =
            "${context.externalCacheDir?.absolutePath}/log".also {
                val file = File(it)
                if (!file.exists()) file.mkdirs()
            }
    }

    val file = File("${getWorkPath(get())}/${System.currentTimeMillis()}.log")

    init {
        if (!file.exists()) {
            file.createNewFile()
        }
        file.appendText(
            """
            | ${BuildConfig.APPLICATION_ID}
            | ${RsConfig.LEVEL} [${RsConfig.versionName} (${RsConfig.versionCode})]
            | ${RsConfig.deviceName}
            | ${RsConfig.systemVersion}
            | ${RsConfig.systemStruct}
            |
        """.trimMargin()
        )
    }

    @SuppressLint("SimpleDateFormat")
    private fun log(level: String, tag: String, message: String) {
        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(System.currentTimeMillis())
        file.appendText("$time $level $tag : $message\n")
    }

    override fun error(tag: String, message: String) {
        log("E", tag, message)
    }

    override fun warn(tag: String, message: String) {
        log("W", tag, message)
    }

    override fun info(tag: String, message: String) {
        log("I", tag, message)
    }

    override fun debug(tag: String, message: String) {
        log("D", tag, message)
    }

    override fun output(file: File) {
        if (file.exists()) file.delete()
        val parentFile = file.parentFile
        ZipOutputStream(file.outputStream()).use { zip ->
            File(getWorkPath(get())).listFiles()?.forEach {
                if (!it.name.endsWith(".log")) return@forEach
                val entry = ZipEntry(it.name)
                zip.putNextEntry(entry)
                it.inputStream().copyTo(zip)
                zip.closeEntry()
            }
        }
    }
}