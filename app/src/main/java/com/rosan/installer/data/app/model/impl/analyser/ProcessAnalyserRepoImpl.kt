package com.rosan.installer.data.app.model.impl.analyser

import com.rosan.installer.data.app.model.entity.AnalyseEntity
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.error.DataEntityNotSupportError
import com.rosan.installer.data.app.repo.AnalyserRepo
import com.rosan.installer.data.app.util.DataType
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

class ProcessAnalyserRepoImpl : AnalyserRepo {
    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<AnalyseEntity>,
    ): List<AppEntity> {
        val apps = mutableListOf<AppEntity>()
        val supportAnalysers = mapOf(
            DataType.APK to ApkAnalyserRepoImpl(),
            DataType.APKM to ApkmAnalyserRepoImpl(),
            DataType.XAPK to XapkAnalyserRepoImpl()
        )
        entities.groupBy { it.type }.forEach { (type, entities) ->
            val analyser = supportAnalysers[type]
            if (analyser != null) apps.addAll(analyser.doWork(config, entities))
            else entities.forEach { entity ->
                apps.addAll((
                        supportAnalysers.values.find { it.isSupport(entity.data) }
                            ?: throw DataEntityNotSupportError(entity.data)
                        ).doWork(config, listOf(entity)))
            }
        }
        return apps
    }
}