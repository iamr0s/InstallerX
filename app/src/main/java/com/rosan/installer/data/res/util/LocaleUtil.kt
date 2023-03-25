package com.rosan.installer.data.res.util

import java.util.*

/*
* 当前语言是否兼容传参
* 不兼容：-1
* 兼容：>= 0
* 数值越大越兼容
* */
fun Locale.getCompatLevel(locale: Locale): Int {
    var level = 0
    if (this.language.isNotEmpty()) {
        if (this.language != locale.language) return -1
        level += 1
    }
    if (this.country.isNotEmpty()) {
        if (this.country != locale.country) return -1
        level += 1
    }
    if (this.script.isNotEmpty()) {
        if (this.script != locale.script) return -1
        level += 1
    }
    if (this.variant.isNotEmpty()) {
        if (this.variant != locale.variant) return -1
        level += 1
    }
    return level
}

fun Locale.getCompatLevel(locales: List<Locale>): Int {
    var ret = -1
    for ((index, locale) in locales.reversed().withIndex()) {
        val level = this.getCompatLevel(locale)
        if (level == -1) continue
        (index * 10 + level).also {
            if (it > ret) ret = it
        }
    }
    return ret
}