package com.rosan.installer.data.recycle.repo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.internal.closeQuietly
import java.io.Closeable
import kotlin.time.DurationUnit
import kotlin.time.toDuration

abstract class Recycler<T : Closeable> {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private var entity: T? = null

    private var referenceCount = 0

    private var recycleJob: Job? = null

    fun make(): Recyclable<T> {
        synchronized(this) {
            val localEntity = entity ?: onMake().apply {
                entity = this
            }
            referenceCount += 1
            return Recyclable(localEntity, this)
        }
    }

    abstract fun onMake(): T

    fun recycle() {
        synchronized(this) {
            referenceCount -= 1
            if (referenceCount > 0) return
            recycleJob?.cancel()
            recycleJob = coroutineScope.launch {
                delay(15.toDuration(DurationUnit.SECONDS))
                if (referenceCount > 0) return@launch
                recycleForcibly()
            }
        }
    }

    fun recycleForcibly() {
        synchronized(this) {
            referenceCount = 0
            entity?.closeQuietly()
            entity = null
        }
    }
}