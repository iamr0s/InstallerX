package com.rosan.installer.data.app.model.impl.analyser

import android.graphics.drawable.Drawable
import com.rosan.installer.data.app.model.entity.AnalyseEntity
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.app.model.entity.error.DataEntityNotSupportError
import com.rosan.installer.data.app.repo.AnalyserRepo
import com.rosan.installer.data.app.repo.analyser.SupportAnalyserRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okio.use
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.io.File
import java.util.zip.ZipFile

class ApkmAnalyserRepoImpl : SupportAnalyserRepo, AnalyserRepo, KoinComponent {
    override fun isSupport(data: DataEntity): Boolean = kotlin.runCatching {
        if (data !is DataEntity.FileEntity) return false
        ZipFile(data.path).use {
            it.getEntry("info.json") ?: return false
        }
        return true
    }.getOrDefault(false)

    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<AnalyseEntity>
    ): List<AppEntity> {
        val apps = mutableListOf<AppEntity>()
        entities.forEach { entity ->
            if (entity.data !is DataEntity.FileEntity) throw DataEntityNotSupportError(entity.data)
            apps.addAll(doInnerWork(config, entity.data))
        }
        return apps
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun doInnerWork(
        config: ConfigEntity,
        entity: DataEntity.FileEntity
    ): List<AppEntity> = withContext(Dispatchers.IO) {
        val apps = mutableListOf<AppEntity>()
        val json = get<Json>()
        ZipFile(entity.path).use { zip ->
            val manifest =
                json.decodeFromStream<Manifest>(zip.getInputStream(zip.getEntry("info.json")))
            val icon = zip.getEntry("icon.png")?.let {
                Drawable.createFromStream(zip.getInputStream(it), "icon.png")
            }
            zip.entries().iterator().forEach {
                if (it.isDirectory) return@forEach
                if (File(it.name).extension != "apk") return@forEach
                val isSplit = File(it.name).nameWithoutExtension != "base"
                val data = DataEntity.ZipEntity(entity.path, it.name)
                val app = if (!isSplit)
                    AppEntity.MainEntity(
                        data = data,
                        packageName = manifest.packageName,
                        versionCode = manifest.versionCode,
                        versionName = manifest.versionName,
                        label = manifest.label,
                        icon = icon
                    )
                else AppEntity.SplitEntity(
                    data = data,
                    packageName = manifest.packageName,
                    splitName = File(it.name).nameWithoutExtension
                )
                apps.add(app)
            }
        }
        return@withContext apps
    }

    @Serializable
    private data class Manifest(
        @SerialName("pname")
        val packageName: String,
        @SerialName("versioncode")
        private val versionCodeStr: String,
        @SerialName("release_version")
        val releaseVersion: String?,
        @SerialName("app_name")
        val appName: String?,
        @SerialName("apk_title")
        val apkTitle: String?,
        @SerialName("release_title")
        val releaseTitle: String?
    ) {
        val versionCode: Long = versionCodeStr.toLong()
        val versionName: String = releaseVersion ?: ""
        val label: String? = appName ?: apkTitle ?: releaseTitle
    }
}