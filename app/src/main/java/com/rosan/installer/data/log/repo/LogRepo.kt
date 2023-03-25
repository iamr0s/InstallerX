package com.rosan.installer.data.log.repo

import java.io.File

interface LogRepo {
    fun error(tag: String, message: String)

    fun warn(tag: String, message: String)

    fun info(tag: String, message: String)

    fun debug(tag: String, message: String)

    fun output(file: File)
}