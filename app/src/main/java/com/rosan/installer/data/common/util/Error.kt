package com.rosan.installer.data.common.util

import java.io.ByteArrayOutputStream
import java.io.PrintStream

fun Throwable.errorInfo(): String = ByteArrayOutputStream().also {
    printStackTrace(PrintStream(it))
}.toByteArray().decodeToString()