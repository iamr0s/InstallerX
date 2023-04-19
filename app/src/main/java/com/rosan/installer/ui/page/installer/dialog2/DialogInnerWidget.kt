package com.rosan.installer.ui.page.installer.dialog2

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import com.rosan.installer.data.installer.repo.InstallerRepo
import com.rosan.installer.ui.page.installer.dialog2.inner.*

// change the content when the id been changed
@OptIn(ExperimentalAnimationApi::class)
fun dialogInnerWidget(
    installer: InstallerRepo,
    params: DialogInnerParams
): @Composable (() -> Unit)? =
    if (params.content == null) null
    else {
        {
            AnimatedContent(
                targetState = "${installer.id}_${params.id}"
            ) {
                params.content.invoke()
            }
        }
    }

@Composable
fun DialogGenerateParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): DialogParams {
    Log.e("r0s", "${viewModel.state}")
    return when (viewModel.state) {
        is DialogViewState.Ready -> ReadyDialog(installer, viewModel)
        is DialogViewState.Resolving -> ResolvingDialog(installer, viewModel)
        is DialogViewState.ResolveFailed -> ResolveFailedDialog(installer, viewModel)
        is DialogViewState.Analysing -> AnalysingDialog(installer, viewModel)
        is DialogViewState.AnalyseFailed -> AnalyseFailedDialog(installer, viewModel)
        is DialogViewState.InstallChoice -> InstallChoiceDialog(installer, viewModel)
        is DialogViewState.InstallPrepare -> InstallPrepareDialog(installer, viewModel)
        is DialogViewState.Installing -> InstallingDialog(installer, viewModel)
        is DialogViewState.InstallSuccess -> InstallSuccessDialog(installer, viewModel)
        is DialogViewState.InstallFailed -> InstallFailedDialog(installer, viewModel)
        else -> ReadyDialog(installer, viewModel)
    }
}