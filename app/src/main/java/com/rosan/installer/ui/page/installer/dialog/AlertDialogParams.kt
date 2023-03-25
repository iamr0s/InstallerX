package com.rosan.installer.ui.page.installer.dialog

import androidx.compose.runtime.Composable

data class AlertDialogParams(
    val leftIcon: @Composable (() -> Unit)? = null,
    val centerIcon: @Composable (() -> Unit)? = null,
    val rightIcon: @Composable (() -> Unit)? = null,
    val leftTitle: @Composable (() -> Unit)? = null,
    val centerTitle: @Composable (() -> Unit)? = null,
    val rightTitle: @Composable (() -> Unit)? = null,
    val leftText: @Composable (() -> Unit)? = null,
    val centerText: @Composable (() -> Unit)? = null,
    val rightText: @Composable (() -> Unit)? = null,
    val leftButton: @Composable (() -> Unit)? = null,
    val centerButton: @Composable (() -> Unit)? = null,
    val rightButton: @Composable (() -> Unit)? = null
)