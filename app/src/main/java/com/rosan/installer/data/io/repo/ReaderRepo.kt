package com.rosan.installer.data.io.repo

import java.io.Closeable

// Resources.h use unsigned number

interface ReaderRepo : Closeable {
    companion object {
        val DEFAULT_ENDIAN = Endian.Big
    }

    enum class Endian {
        Big,
        Little
    }

    var endian: Endian

    val used: Long

    val byte: Byte

    val uByte: UByte

    val short: Short

    val uShort: UShort

    val int: Int

    val uInt: UInt

    val long: Long

    val uLong: ULong

    fun read(length: Int): ByteArray

    fun read(bytes: ByteArray, offset: Int = 0, length: Int = bytes.size)

    fun skip(length: Int) {
        skip(length.toLong())
    }

    fun skip(length: Long)
}