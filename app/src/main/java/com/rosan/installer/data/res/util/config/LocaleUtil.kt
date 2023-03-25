package com.rosan.installer.data.res.util.config

import com.rosan.installer.data.res.model.impl.res.arsc.TableType
import com.rosan.installer.data.res.util.getCompatLevel
import java.util.*


//fun Iterable<TableType>.filterByLocales(locales: List<Locale>): List<TableType> = filter {
//    it.config.locale.getCompatLevel(locales) != -1
//}

fun Iterable<TableType>.sortedByLocales(locales: List<Locale>): List<TableType> =
    sortedWith { t1, t2 ->
        val o1 = t1.config.locale
        val o2 = t2.config.locale
        -(o1.getCompatLevel(locales) - o2.getCompatLevel(locales))
    }