package com.rosan.installer.data.common.util

import android.content.pm.PackageInfo
import androidx.core.content.pm.PackageInfoCompat

val PackageInfo.compatVersionCode: Long
    get() = PackageInfoCompat.getLongVersionCode(this)