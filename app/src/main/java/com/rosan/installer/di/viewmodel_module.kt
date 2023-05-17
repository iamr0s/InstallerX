package com.rosan.installer.di

import androidx.navigation.NavController
import com.rosan.installer.data.installer.repo.InstallerRepo
import com.rosan.installer.ui.page.installer.dialog.DialogViewModel
import com.rosan.installer.ui.page.settings.config.all.AllViewModel
import com.rosan.installer.ui.page.settings.config.apply.ApplyViewModel
import com.rosan.installer.ui.page.settings.config.edit.EditViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { (installer: InstallerRepo) ->
        com.rosan.installer.ui.page.installer.dialog.DialogViewModel(installer)
    }

    viewModel { (installer: InstallerRepo) ->
        DialogViewModel(installer)
    }

    viewModel { (navController: NavController) ->
        AllViewModel(navController, get())
    }

    viewModel { (id: Long?) ->
        EditViewModel(get(), id)
    }

    viewModel { (id: Long) ->
        ApplyViewModel(get(), get(), id)
    }
}