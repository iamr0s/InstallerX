package com.rosan.installer.ui.page.settings.config.apply

sealed class ApplyViewAction {
    object Init : ApplyViewAction()
    object LoadApps : ApplyViewAction()
    object LoadAppEntities : ApplyViewAction()
    data class ApplyPackageName(
        val packageName: String?,
        val applied: Boolean
    ) : ApplyViewAction()
}