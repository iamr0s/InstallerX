package com.rosan.installer.data.app.model.impl.analyser.apk

import com.rosan.installer.data.app.model.entity.AnalyseEntity
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.app.model.entity.error.DataEntityNotSupportError
import com.rosan.installer.data.app.repo.analyser.AManifestApkAnalyserRepo
import com.rosan.installer.data.common.model.entity.ErrorEntity
import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.repo.ArscRepo
import com.rosan.installer.data.res.repo.AxmlPullRepo
import com.rosan.installer.data.res.util.ResourcesProxy
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import java.io.InputStream
import java.util.zip.ZipFile

class R0sAnalyserRepoImpl : AManifestApkAnalyserRepo {
    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<AnalyseEntity>,
    ): List<AppEntity> = withContext(Dispatchers.IO) {
        val apps = mutableListOf<AppEntity>()
        entities.forEach { entity ->
            if (entity.data !is DataEntity.FileEntity) throw DataEntityNotSupportError(entity.data)
            ZipFile(entity.data.path).use { zip ->
                fun getZipInputStream(name: String): InputStream? {
                    val entry = zip.getEntry(name) ?: return null
                    return zip.getInputStream(entry)
                }

                val arsc = getZipInputStream("resources.arsc")?.buffered()?.readerRepo()?.use {
                    it.endian = ReaderRepo.Endian.Little
                    get<ArscRepo> {
                        parametersOf(it)
                    }
                }!!

                val resources = object : ResourcesProxy(arsc) {
                    override fun loadBytes(path: String): ByteArray? {
                        return loadInputStream(path)?.readBytes()
                    }

                    override fun loadInputStream(path: String): InputStream? {
                        return getZipInputStream(path)
                    }
                }

                val manifestInputStream =
                    getZipInputStream("AndroidManifest.xml")?.buffered()?.readerRepo()?.also {
                        it.endian = ReaderRepo.Endian.Little
                    } ?: throw ErrorEntity("not android package")

                apps.add(loadAppEntity(
                    entity,
                    resources,
                    null,
                    get<AxmlPullRepo> {
                        parametersOf(manifestInputStream)
                    }
                ))
            }
        }
        return@withContext apps
    }
}