package com.rosan.installer.ui.page.settings.config.apply

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.installer.data.settings.model.room.entity.AppEntity
import com.rosan.installer.data.settings.repo.AppRepo
import com.rosan.installer.data.settings.repo.ConfigRepo
import com.rosan.installer.ui.common.ViewContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ApplyViewModel(
    private val configRepo: ConfigRepo,
    private val appRepo: AppRepo,
    private val id: Long
) : ViewModel(), KoinComponent {
    private val context by inject<Context>()

    var state by mutableStateOf(
        ApplyViewState(
            apps = ViewContent(
                data = emptyList(),
                progress = ViewContent.Progress.Loading
            ),
            appEntities = ViewContent(
                data = emptyList(),
                progress = ViewContent.Progress.Loading
            )
        )
    )

    fun dispatch(action: ApplyViewAction) {
        when (action) {
            is ApplyViewAction.Init -> init()
            is ApplyViewAction.LoadApps -> loadApps()
            is ApplyViewAction.LoadAppEntities -> collectAppEntities()
            is ApplyViewAction.ApplyPackageName -> applyPackageName(
                action.packageName,
                action.applied
            )
        }
    }

    private var inited = false

    private fun init() {
        if (inited) return
        inited = true
        loadApps()
        collectAppEntities()
    }

    private var loadAppsJob: Job? = null

    private fun loadApps() {
        loadAppsJob?.cancel()
        loadAppsJob = viewModelScope.launch(Dispatchers.IO) {
            state = state.copy(
                apps = state.apps.copy(
                    progress = ViewContent.Progress.Loading
                )
            )
            val list = mutableListOf<ApplyViewAppInfo>()
            context.packageManager?.let { packageManager ->
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
                else packageManager.getInstalledPackages(0))
            }?.forEach {
                ApplyViewAppInfo.buildOrNull(context, it)?.let {
                    list.add(it)
                }
            }
            state = state.copy(
                apps = state.apps.copy(
                    data = list,
                    progress = ViewContent.Progress.Loaded
                )
            )
        }
    }

    private var collectAppEntitiesJob: Job? = null

    private fun collectAppEntities() {
        collectAppEntitiesJob?.cancel()
        collectAppEntitiesJob = viewModelScope.launch(Dispatchers.IO) {
            state = state.copy(
                appEntities = state.appEntities.copy(
                    progress = ViewContent.Progress.Loading
                )
            )
            appRepo.flowAll().collect {
                state = state.copy(
                    appEntities = state.appEntities.copy(
                        data = it.filter { it.configId == id },
                        progress = ViewContent.Progress.Loaded
                    )
                )
            }
        }
    }

    private fun applyPackageName(packageName: String?, applied: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = appRepo.findByPackageName(packageName)
            if (applied) {
                if (entity != null) {
                    entity.configId = id
                    appRepo.update(entity)
                } else {
                    appRepo.insert(
                        AppEntity(
                            packageName = packageName,
                            configId = id
                        )
                    )
                }
            } else {
                entity?.let { appRepo.delete(it) }
            }
        }
    }
}