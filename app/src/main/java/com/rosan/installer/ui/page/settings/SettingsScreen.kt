package com.rosan.installer.ui.page.settings

sealed class SettingsScreen(val route: String) {
    object Main : SettingsScreen("main")
    object EditConfig : SettingsScreen("config/edit?id={id}")
    object ApplyConfig : SettingsScreen("config/apply?id={id}")

    sealed class Builder(val route: String) {
        object Main : SettingsScreen("main")
        class EditConfig(id: Long? = null) : SettingsScreen(
            "config/edit?id=${id ?: -1}"
        )

        class ApplyConfig(id: Long) : SettingsScreen(
            "config/apply?id=$id"
        )
    }
}
