package com.rosan.installer.data.res.util.config

import com.rosan.installer.data.res.model.impl.res.arsc.TableType
import com.rosan.installer.data.res.model.impl.res.arsc.config.UIModeType

fun UIModeType.getCompatLevel(uiModeType: UIModeType): Int {
    if (this == uiModeType) return 1
    if (this == UIModeType.Any) return 0
    return -1
}

fun Iterable<TableType>.sortedByUIModeType(uiModeType: UIModeType): List<TableType> =
    sortedWith { t1, t2 ->
        val o1 = t1.config.uiModeType
        val o2 = t2.config.uiModeType
        val o1Level = o1.getCompatLevel(uiModeType)
        val o2Level = o2.getCompatLevel(uiModeType)
        -(o1Level - o2Level)
    }
