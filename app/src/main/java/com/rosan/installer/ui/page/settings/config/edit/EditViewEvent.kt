package com.rosan.installer.ui.page.settings.config.edit

sealed class EditViewEvent {
    data class SnackBar(val message: String) : EditViewEvent()
    object Saved : EditViewEvent()
}