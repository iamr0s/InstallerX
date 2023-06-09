package com.rosan.installer.ui.page.settings.preferred

import com.rosan.installer.data.settings.model.room.entity.ConfigEntity

data class PreferredViewState(
    val authorizer: ConfigEntity.Authorizer = ConfigEntity.Authorizer.Shizuku,
    val customizeAuthorizer: String = ""
) {
    val authorizerCustomize = authorizer == ConfigEntity.Authorizer.Customize
}