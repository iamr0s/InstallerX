package com.rosan.installer.ui.page.settings.config.apply

import android.graphics.drawable.Drawable
import androidx.compose.runtime.MutableState

data class ApplyViewApp(
    val packageName: String,
    val versionName: String?,
    val versionCode: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val isSystemApp: Boolean,
    val label: String?
)