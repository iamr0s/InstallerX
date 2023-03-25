package com.rosan.installer.ui.page.settings.config.apply

import com.rosan.installer.data.settings.model.room.entity.AppEntity
import com.rosan.installer.ui.common.ViewContent

data class ApplyViewState(
    val apps: ViewContent<List<ApplyViewAppInfo>>,
    val appEntities: ViewContent<List<AppEntity>>,
) {
}