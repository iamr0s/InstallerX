package com.rosan.installer.data.res.util.config

import com.rosan.installer.data.res.model.impl.res.arsc.TableType

private fun Int.getCompatLevel(sdkVersion: Int): Int {
    if (this > sdkVersion) return -1
    return this
}

fun Iterable<TableType>.sortedBySdkVersion(sdkVersion: Int): List<TableType> =
    sortedWith { t1, t2 ->
        val o1 = t1.config.sdkVersion.toInt()
        val o2 = t2.config.sdkVersion.toInt()
        val o1Level = o1.getCompatLevel(sdkVersion)
        val o2Level = o2.getCompatLevel(sdkVersion)
        -(o1Level - o2Level)
    }
