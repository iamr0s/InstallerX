package com.rosan.installer.data.app.model.impl.analyser

import com.rosan.installer.data.app.model.entity.AnalyseEntity
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.app.model.impl.analyser.apk.BaseAnalyserRepoImpl
import com.rosan.installer.data.app.repo.AnalyserRepo
import com.rosan.installer.data.app.repo.analyser.SupportAnalyserRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import java.util.zip.ZipFile

class ApkAnalyserRepoImpl : SupportAnalyserRepo, AnalyserRepo {
    override fun isSupport(data: DataEntity): Boolean = kotlin.runCatching {
        if (data !is DataEntity.FileEntity) return false
        ZipFile(data.path).use {
            it.getEntry("AndroidManifest.xml") ?: return false
        }
        return true
    }.getOrDefault(false)

//    override suspend fun doWork(
//        config: ConfigEntity,
//        entities: List<AnalyseEntity>,
//    ): List<AppEntity> = when (config.analyser) {
//        ConfigEntity.Analyser.R0s -> R0sAnalyserRepoImpl().doWork(config, entities)
//        ConfigEntity.Analyser.System -> SystemAnalyserRepoImpl().doWork(config, entities)
//    }

//    override suspend fun doWork(
//        config: ConfigEntity,
//        entities: List<AnalyseEntity>,
//    ): List<AppEntity> = if (config.compatMode) CompatAnalyserRepoImpl().doWork(config, entities)
//    else SystemAnalyserRepoImpl().doWork(config, entities)

    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<AnalyseEntity>,
    ): List<AppEntity> = BaseAnalyserRepoImpl().doWork(config, entities)
}