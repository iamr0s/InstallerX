package com.rosan.installer.data.res.util.config

import com.rosan.installer.data.res.model.impl.res.arsc.TableType
import com.rosan.installer.data.res.model.impl.res.arsc.config.Density
import kotlin.math.absoluteValue


fun Iterable<TableType>.sortedByDensity(density: Int): List<TableType> = sortedWith { t1, t2 ->
    var requestDensity = density
    if (requestDensity == Density.Default.value
        || requestDensity == Density.None.value
        || requestDensity == Density.Any.value
    ) requestDensity = Density.Medium.value
    val o1Density = t1.config.density
    val o2Density = t2.config.density
    if (o1Density == Density.Any.value || o2Density == Density.Any.value)
        return@sortedWith -(o1Density - o2Density)
    val o1Level = (o1Density - requestDensity).absoluteValue
    val o2Level = (o2Density - requestDensity).absoluteValue
    return@sortedWith o1Level - o2Level
}