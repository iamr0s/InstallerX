package com.rosan.installer.ui.page.installer.dialog

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.installer.data.installer.model.entity.ProgressEntity
import com.rosan.installer.data.installer.repo.InstallerRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DialogViewModel(
    private var repo: InstallerRepo
) : ViewModel(), KoinComponent {
    private val context by inject<Context>()

    var state by mutableStateOf<DialogViewState>(DialogViewState.Ready)
        private set

    fun dispatch(action: DialogViewAction) {
        when (action) {
            is DialogViewAction.CollectRepo -> collectRepo(action.repo)
            is DialogViewAction.Close -> close()
            is DialogViewAction.Analyse -> analyse()
            is DialogViewAction.InstallChoice -> installChoice()
            is DialogViewAction.InstallPrepare -> installPrepare()
            is DialogViewAction.Install -> install()
            is DialogViewAction.Background -> background()
        }
    }

    private var collectRepoJob: Job? = null

    private fun collectRepo(repo: InstallerRepo) {
        this.repo = repo
        collectRepoJob?.cancel()
        collectRepoJob = viewModelScope.launch {
            repo.progress.collect { progress ->
                state = when (progress) {
                    is ProgressEntity.Ready -> DialogViewState.Ready
                    is ProgressEntity.Resolving -> DialogViewState.Resolving
                    is ProgressEntity.ResolvedFailed -> DialogViewState.ResolveFailed
                    is ProgressEntity.Analysing -> DialogViewState.Analysing
                    is ProgressEntity.AnalysedFailed -> DialogViewState.AnalyseFailed
                    is ProgressEntity.AnalysedSuccess ->
                        if (repo.entities.count { it.selected } != 1) DialogViewState.InstallChoice
                        else DialogViewState.InstallPrepare
                    is ProgressEntity.Installing -> DialogViewState.Installing
                    is ProgressEntity.InstallFailed -> DialogViewState.InstallFailed
                    is ProgressEntity.InstallSuccess -> DialogViewState.InstallSuccess
                    else -> DialogViewState.Ready
                }
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun toast(@StringRes resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_LONG).show()
    }

    private fun close() {
        repo.close()
    }

    private fun analyse() {
        repo.analyse()
    }

    private fun installChoice() {
        state = DialogViewState.InstallChoice
    }

    private fun installPrepare() {
        state = DialogViewState.InstallPrepare
    }

    private fun install() {
        repo.install()
    }

    private fun background() {
        repo.background(true)
    }
}