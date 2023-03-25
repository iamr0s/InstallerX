package com.rosan.installer.data.res.util.config

import com.rosan.installer.data.res.model.impl.res.arsc.TableType
import com.rosan.installer.data.res.model.impl.res.arsc.config.Orientation

fun Orientation.getCompatLevel(orientation: Orientation): Int {
    if (this == orientation) return 1
    if (this == Orientation.Any) return 0
    return -1
}

fun Iterable<TableType>.sortedByOrientation(orientation: Orientation): List<TableType> =
    sortedWith { t1, t2 ->
        val o1 = t1.config.orientation
        val o2 = t2.config.orientation
        val o1Level = o1.getCompatLevel(orientation)
        val o2Level = o2.getCompatLevel(orientation)
        -(o1Level - o2Level)
    }
