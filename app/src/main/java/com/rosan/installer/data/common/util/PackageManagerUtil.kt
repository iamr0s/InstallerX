package com.rosan.installer.data.common.util

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat

val PackageInfo.compatVersionCode: Long
    get() = PackageInfoCompat.getLongVersionCode(this)

fun PackageManager.getCompatInstalledPackages(flags: Int): List<PackageInfo> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
    else getInstalledPackages(flags)
}