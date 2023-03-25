package com.rosan.installer.data.io.model.impl

import com.rosan.installer.data.io.repo.ReaderRepo

private class ProxyReaderRepoImpl(private val reader: ReaderRepo) : ReaderRepo {
    private var _used: Long = 0

    override var endian: ReaderRepo.Endian
        get() = reader.endian
        set(value) {
            reader.endian = value
        }

    override val used: Long
        get() = _used

    override val byte: Byte
        get() {
            val used = reader.used
            val ret = reader.byte
            this._used += reader.used - used
            return ret
        }

    override val uByte: UByte
        get() {
            val used = reader.used
            val ret = reader.uByte
            this._used += reader.used - used
            return ret
        }

    override val short: Short
        get() {
            val used = reader.used
            val ret = reader.short
            this._used += reader.used - used
            return ret
        }

    override val uShort: UShort
        get() {
            val used = reader.used
            val ret = reader.uShort
            this._used += reader.used - used
            return ret
        }

    override val int: Int
        get() {
            val used = reader.used
            val ret = reader.int
            this._used += reader.used - used
            return ret
        }

    override val uInt: UInt
        get() {
            val used = reader.used
            val ret = reader.uInt
            this._used += reader.used - used
            return ret
        }

    override val long: Long
        get() {
            val used = reader.used
            val ret = reader.long
            this._used += reader.used - used
            return ret
        }

    override val uLong: ULong
        get() {
            val used = reader.used
            val ret = reader.uLong
            this._used += reader.used - used
            return ret
        }

    override fun read(length: Int): ByteArray {
        val used = reader.used
        val ret = reader.read(length)
        this._used += reader.used - used
        return ret
    }

    override fun read(bytes: ByteArray, offset: Int, length: Int) {
        val used = reader.used
        val ret = reader.read(bytes, offset, length)
        this._used += reader.used - used
        return ret
    }

    override fun skip(length: Long) {
        val used = reader.used
        val ret = reader.skip(length)
        this._used += reader.used - used
        return ret
    }

    override fun close() {
        reader.close()
    }
}

fun ReaderRepo.readerRepo(): ReaderRepo {
    return ProxyReaderRepoImpl(this)
}