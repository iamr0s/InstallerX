package com.rosan.installer.data.console.util

import com.rosan.installer.data.console.model.impl.DefaultConsoleBuilderRepoImpl
import com.rosan.installer.data.console.model.impl.ShizukuConsoleBuilderRepoImpl
import com.rosan.installer.data.console.repo.ConsoleRepo

class ConsoleRepoUtil {
    companion object {
        suspend fun open(action: DefaultConsoleBuilderRepoImpl.() -> Unit): ConsoleRepo {
            val repo = DefaultConsoleBuilderRepoImpl()
            repo.action()
            return repo.open()
        }

        suspend fun sh(action: DefaultConsoleBuilderRepoImpl.() -> Unit): ConsoleRepo {
            val repo = DefaultConsoleBuilderRepoImpl()
            repo.command("sh")
            repo.action()
            return repo.open()
        }

        suspend fun su(action: DefaultConsoleBuilderRepoImpl.() -> Unit): ConsoleRepo {
            val repo = DefaultConsoleBuilderRepoImpl()
            repo.command("su", "--mount-master")
            repo.action()
            return repo.open()
        }

        suspend fun system(action: DefaultConsoleBuilderRepoImpl.() -> Unit): ConsoleRepo {
            val repo = DefaultConsoleBuilderRepoImpl()
            // su 1000
            // su system
            // su --login 1000
            // su --login system
            // su -l 1000
            // su -l system
            repo.command("su", "1000")
            repo.action()
            return repo.open()
        }

        suspend fun shizuku(action: ShizukuConsoleBuilderRepoImpl.() -> Unit): ConsoleRepo {
            val repo = ShizukuConsoleBuilderRepoImpl()
            repo.command("sh")
            repo.action()
            return repo.open()
        }
    }
}