package com.rosan.installer.ui.page.installer

import androidx.compose.runtime.Composable
import com.rosan.installer.data.installer.repo.InstallerRepo
import com.rosan.installer.ui.page.installer.dialog2.DialogPage

@Composable
fun InstallerPage(installer: InstallerRepo) {
    DialogPage(installer = installer)
}