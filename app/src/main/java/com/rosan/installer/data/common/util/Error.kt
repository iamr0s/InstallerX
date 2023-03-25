package com.rosan.installer.data.common.util

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset

fun Throwable.errorInfo(): String = ByteArrayOutputStream().also {
    PrintStream(it).also {
        this.printStackTrace(it)
    }
}.toByteArray().toString(Charset.defaultCharset())