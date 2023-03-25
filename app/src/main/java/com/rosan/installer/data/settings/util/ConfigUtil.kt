package com.rosan.installer.data.settings.util

import com.rosan.installer.data.settings.model.room.entity.AppEntity
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import com.rosan.installer.data.settings.repo.AppRepo
import com.rosan.installer.data.settings.repo.ConfigRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ConfigUtil {
    companion object : KoinComponent {
        suspend fun getByPackageName(packageName: String? = null): ConfigEntity =
            withContext(Dispatchers.IO) {
                val repo = get<ConfigRepo>()
                val app = getAppByPackageName(packageName)
                var config: ConfigEntity? = null
                if (app != null) config = repo.find(app.configId)
                if (config != null) return@withContext config
                config = repo.all().firstOrNull()
                if (config != null) return@withContext config
                return@withContext ConfigEntity.default
            }

        private suspend fun getAppByPackageName(packageName: String? = null): AppEntity? =
            withContext(Dispatchers.IO) {
                val repo = get<AppRepo>()
                var app: AppEntity? = repo.findByPackageName(packageName)
                if (app != null) return@withContext app
                if (packageName != null) app = repo.findByPackageName(null)
                if (app != null) return@withContext app
                return@withContext null
            }
    }
}