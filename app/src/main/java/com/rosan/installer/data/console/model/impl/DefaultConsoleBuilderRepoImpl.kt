package com.rosan.installer.data.console.model.impl

import com.rosan.installer.data.console.repo.ConsoleBuilderRepo
import com.rosan.installer.data.console.repo.ConsoleRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

open class DefaultConsoleBuilderRepoImpl : ConsoleBuilderRepo() {
    // impl it, use ProcessBuilder
    override suspend fun open(): ConsoleRepo {
        return ConsoleRepoImpl(
            withContext(Dispatchers.IO) {
                val builder = ProcessBuilder(command)
                environment?.let {
                    builder.environment().clear()
                    builder.environment().putAll(strings2environment(it))
                }
                directory?.let {
                    builder.directory(File(it))
                }
                builder.start()
            }
        )
    }

    // impl it, use Runtime.exec
    suspend fun open2(): ConsoleRepo {
        return ConsoleRepoImpl(
            withContext(Dispatchers.IO) {
                Runtime.getRuntime()
                    .exec(
                        command.toTypedArray(),
                        environment?.toTypedArray() ?: emptyArray(),
                        directory?.let { File(it) }
                    )
            }
        )
    }
}
