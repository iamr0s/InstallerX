package com.rosan.installer.data.app.model.impl.analyser

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.rosan.installer.data.app.model.entity.AnalyseExtraEntity
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.app.repo.AnalyserRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import java.io.File
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

object ApksAnalyserRepoImpl : AnalyserRepo {
    override suspend fun doWork(
        config: ConfigEntity, data: List<DataEntity>, extra: AnalyseExtraEntity
    ): List<AppEntity> {
        val apps = mutableListOf<AppEntity>()
        data.forEach { apps.addAll(doWork(config, it, extra)) }
        return apps
    }

    private suspend fun doWork(
        config: ConfigEntity, data: DataEntity, extra: AnalyseExtraEntity
    ): List<AppEntity> {
        return when (data) {
            is DataEntity.FileEntity -> doFileWork(config, data, extra)
            is DataEntity.FileDescriptorEntity -> doFileDescriptorWork(config, data, extra)
            is DataEntity.ZipFileEntity -> doZipFileWork(config, data, extra)
            is DataEntity.ZipInputStreamEntity -> doZipInputStreamWork(config, data, extra)
        }
    }

    private suspend fun doFileWork(
        config: ConfigEntity, data: DataEntity.FileEntity, extra: AnalyseExtraEntity
    ): List<AppEntity> {
        val apps = mutableListOf<AppEntity>()
        ZipFile(data.path).use {
            val entries = it.entries().toList()
            for (entry in entries) {
                if (File(entry.name).extension.toLowerCase(Locale.current) != "apk") continue
                it.getInputStream(entry).use {
                    apps.addAll(
                        doApkInInputStreamWork(
                            config,
                            DataEntity.ZipFileEntity(entry.name, data),
                            it,
                            extra
                        )
                    )
                }
            }
        }
        return apps
    }

    private suspend fun doFileDescriptorWork(
        config: ConfigEntity, data: DataEntity.FileDescriptorEntity, extra: AnalyseExtraEntity
    ): List<AppEntity> =
        doZipInputStreamWork(config, data, ZipInputStream(data.getInputStream()), extra)

    private suspend fun doZipFileWork(
        config: ConfigEntity, data: DataEntity.ZipFileEntity, extra: AnalyseExtraEntity
    ): List<AppEntity> =
        doZipInputStreamWork(config, data, ZipInputStream(data.getInputStream()), extra)

    private suspend fun doZipInputStreamWork(
        config: ConfigEntity, data: DataEntity.ZipInputStreamEntity, extra: AnalyseExtraEntity
    ): List<AppEntity> =
        doZipInputStreamWork(config, data, ZipInputStream(data.getInputStream()), extra)

    private suspend fun doZipInputStreamWork(
        config: ConfigEntity, data: DataEntity, zip: ZipInputStream, extra: AnalyseExtraEntity
    ): List<AppEntity> {
        val apps = mutableListOf<AppEntity>()
        zip.use {
            while (true) {
                val entry = zip.nextEntry ?: break
                if (File(entry.name).extension.toLowerCase(Locale.current) != "apk") continue
                apps.addAll(
                    doApkInInputStreamWork(
                        config,
                        DataEntity.ZipInputStreamEntity(entry.name, data),
                        it,
                        extra
                    )
                )
            }
        }
        return apps
    }

    private suspend fun doApkInInputStreamWork(
        config: ConfigEntity, data: DataEntity, inputStream: InputStream, extra: AnalyseExtraEntity
    ): List<AppEntity> {
        val file =
            File.createTempFile(UUID.randomUUID().toString(), null, File(extra.cacheDirectory))
        file.outputStream().use {
            inputStream.copyTo(it)
        }
        val tempData = DataEntity.FileEntity(file.absolutePath)
        tempData.source = data
        val result = kotlin.runCatching {
            ApkAnalyserRepoImpl.doWork(
                config,
                listOf(tempData),
                extra
            )
        }
        result.onSuccess {
            return it
        }
        file.deleteRecursively()
        return emptyList()
    }
}