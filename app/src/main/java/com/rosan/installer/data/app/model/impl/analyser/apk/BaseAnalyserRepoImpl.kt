package com.rosan.installer.data.app.model.impl.analyser.apk

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.rosan.installer.data.app.model.entity.AnalyseEntity
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.app.model.entity.error.DataEntityNotSupportError
import com.rosan.installer.data.app.repo.AnalyserRepo
import com.rosan.installer.data.common.util.compatVersionCode
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BaseAnalyserRepoImpl : AnalyserRepo, KoinComponent {
    private val context by inject<Context>()

    private val packageManager = context.packageManager

    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<AnalyseEntity>
    ): List<AppEntity> {
        val apps = mutableListOf<AppEntity>()
        val maybeSplitApks = mutableListOf<AnalyseEntity>()
        entities.forEach { entity ->
            if (entity.data !is DataEntity.FileEntity) throw DataEntityNotSupportError(entity.data)
            val path = entity.data.path
            val packageInfo =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    packageManager.getPackageArchiveInfo(
                        path,
                        PackageManager.PackageInfoFlags.of(0L)
                    )
                else
                    packageManager.getPackageArchiveInfo(
                        path,
                        0
                    )
            if (packageInfo == null) {
                maybeSplitApks.add(entity)
                return@forEach
            }
            val applicationInfo = packageInfo.applicationInfo
            applicationInfo.sourceDir = path
            applicationInfo.publicSourceDir = path
            apps.add(
                AppEntity.MainEntity(
                    data = entity.data,
                    packageName = packageInfo.packageName,
                    versionCode = packageInfo.compatVersionCode,
                    versionName = packageInfo.versionName,
                    label = applicationInfo.loadLabel(packageManager).toString(),
                    icon = applicationInfo.loadIcon(packageManager)
                )
            )
        }

        // :(
        // Android getPackageArchiveInfo not support split apk
        apps.addAll(SplitAnalyserRepoImpl().doWork(config, maybeSplitApks))
        return apps
    }
}