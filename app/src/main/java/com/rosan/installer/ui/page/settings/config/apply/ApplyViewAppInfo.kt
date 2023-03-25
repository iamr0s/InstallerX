package com.rosan.installer.ui.page.settings.config.apply

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import coil.executeBlocking
import coil.imageLoader
import coil.request.ImageRequest
import com.rosan.installer.data.common.util.compatVersionCode
import org.koin.core.component.KoinComponent

data class ApplyViewAppInfo(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val label: String?,
    val icon: ImageRequest
) {
    companion object : KoinComponent {
        fun build(context: Context, packageName: String): ApplyViewAppInfo {
            return buildOrNull(context, packageName) ?: ApplyViewAppInfo(
                packageName = packageName,
                versionName = "not installed",
                versionCode = -1,
                label = "Uninstalled",
                icon = context.imageLoader.executeBlocking(
                    ImageRequest.Builder(context)
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .build()
                ).request
            )
        }

        fun buildOrNull(context: Context, packageName: String): ApplyViewAppInfo? {
            val packageManager = context.packageManager ?: return null
            val packageInfo = kotlin.runCatching {
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    context.packageManager?.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(
                            0
                        )
                    )
                else context.packageManager?.getPackageInfo(packageName, 0))
            }.getOrNull() ?: return null
            return buildOrNull(context, packageInfo)
        }

        fun buildOrNull(context: Context, packageInfo: PackageInfo): ApplyViewAppInfo? {
            val packageManager = context.packageManager ?: return null
            val applicationInfo = packageInfo.applicationInfo ?: return null
            return ApplyViewAppInfo(
                packageName = packageInfo.packageName,
                versionName = packageInfo.versionName ?: "",
                versionCode = packageInfo.compatVersionCode,
                label = applicationInfo.loadLabel(packageManager).toString(),
                icon = context.imageLoader.executeBlocking(
                    ImageRequest.Builder(context)
                        .data(applicationInfo.loadIcon(packageManager))
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .build()
                ).request
            )
        }
    }
}