package com.rosan.installer.data.recycle.repo

import java.io.Closeable

class Recyclable<T>(
    // you can't use this after recycle func called
    val entity: T,
    private val recycler: Recycler<*>,
) : Closeable {
    private var recycled = false

    fun recycle() {
        synchronized(this) {
            if (recycled) return
            recycled = true
            recycler.recycle()
        }
    }

    override fun close() {
        recycle()
    }
}