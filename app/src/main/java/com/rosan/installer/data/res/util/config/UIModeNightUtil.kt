package com.rosan.installer.data.res.util.config

import com.rosan.installer.data.res.model.impl.res.arsc.TableType
import com.rosan.installer.data.res.model.impl.res.arsc.config.UIModeNight

fun UIModeNight.getCompatLevel(uiModeNight: UIModeNight): Int {
    if (this == uiModeNight) return 1
    if (this == UIModeNight.Any) return 0
    return -1
}

fun Iterable<TableType>.sortedByUIModeNight(uiModeNight: UIModeNight): List<TableType> =
    sortedWith { t1, t2 ->
        val o1 = t1.config.uiModeNight
        val o2 = t2.config.uiModeNight
        val o1Level = o1.getCompatLevel(uiModeNight)
        val o2Level = o2.getCompatLevel(uiModeNight)
        -(o1Level - o2Level)
    }
