package com.rosan.installer.data.app.model.impl

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.rosan.installer.data.app.model.entity.AnalyseExtraEntity
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.app.model.impl.analyser.ApkAnalyserRepoImpl
import com.rosan.installer.data.app.model.impl.analyser.ApkMAnalyserRepoImpl
import com.rosan.installer.data.app.model.impl.analyser.ApksAnalyserRepoImpl
import com.rosan.installer.data.app.model.impl.analyser.XApkAnalyserRepoImpl
import com.rosan.installer.data.app.repo.AnalyserRepo
import com.rosan.installer.data.app.util.DataType
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import java.io.File
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

object AnalyserRepoImpl : AnalyserRepo {
    override suspend fun doWork(
        config: ConfigEntity,
        data: List<DataEntity>,
        extra: AnalyseExtraEntity
    ): List<AppEntity> {
        val apps = mutableListOf<AppEntity>()

        val analysers = mapOf(
            DataType.APK to ApkAnalyserRepoImpl,
            DataType.APKS to ApksAnalyserRepoImpl,
            DataType.APKM to ApkMAnalyserRepoImpl,
            DataType.XAPK to XApkAnalyserRepoImpl
        )
        val tasks = mutableMapOf<AnalyserRepo, MutableList<DataEntity>>()
        data.forEach {
            val type = kotlin.runCatching { getDataType(config, it) ?: DataType.APK }
                .getOrDefault(DataType.APK)
            val analyser =
                analysers[type] ?: throw Exception("can't found analyser for this data: '$data'")
            val value = tasks[analyser] ?: mutableListOf()
            value.add(it)
            tasks[analyser] = value
        }
        for ((key, value) in tasks) {
            apps.addAll(key.doWork(config, value, extra))
        }
        return apps
    }

    private fun getDataType(config: ConfigEntity, data: DataEntity): DataType? {
        return when (data) {
            is DataEntity.FileEntity -> ZipFile(data.path).use {
                when {
                    it.getEntry("AndroidManifest.xml") != null -> DataType.APK
                    it.getEntry("info.json") != null -> DataType.APKM
                    it.getEntry("manifest.json") != null -> DataType.XAPK
                    else -> {
                        val entries = it.entries().toList()
                        var containsApk = false
                        for (entry in entries) {
                            if (File(entry.name).extension.toLowerCase(Locale.current) != "apk") continue
                            containsApk = true
                            break
                        }
                        if (containsApk) DataType.APKS else null
                    }
                }
            }

            else -> ZipInputStream(data.getInputStream()).use { zip ->
                var type: DataType? = null
                var containsApk = false
                while (true) {
                    val entry = zip.nextEntry ?: break
                    type = when (entry.name) {
                        "AndroidManifest.xml" -> DataType.APK
                        "info.json" -> DataType.APKM
                        "manifest.json" -> DataType.XAPK
                        else -> null
                    }
                    if (File(entry.name).extension.toLowerCase(Locale.current) == "apk") containsApk =
                        true
                    zip.closeEntry()
                    if (type != null) break
                }
                if (type == null && containsApk) type = DataType.APKS
                return@use type
            }
        }
    }
}