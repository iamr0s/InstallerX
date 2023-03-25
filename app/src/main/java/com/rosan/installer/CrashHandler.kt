package com.rosan.installer

import com.rosan.installer.data.common.util.errorInfo
import com.rosan.installer.data.log.model.impl.FileLogRepoImpl
import java.lang.Thread.UncaughtExceptionHandler

class CrashHandler : UncaughtExceptionHandler {
    companion object {
        private var isInited = false

        private var defaultHandler: UncaughtExceptionHandler? = null

        fun init() {
            synchronized(this) {
                if (isInited) return
                isInited = true
                defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
                Thread.setDefaultUncaughtExceptionHandler(CrashHandler())
            }
        }
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        FileLogRepoImpl().error("thread ${t.id}", e.errorInfo())
        defaultHandler?.uncaughtException(t, e)
    }
}