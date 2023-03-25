package com.rosan.installer.data.common.util

fun <T> MutableCollection<in T>.addAll(vararg elements: T): Boolean {
    return addAll(elements.asList())
}