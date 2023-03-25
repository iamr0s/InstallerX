package com.rosan.installer.data.io.model.impl

import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.io.util.*
import java.io.InputStream

private class StreamReaderRepoImpl(private val input: InputStream) : ReaderRepo {
    private var _endian = ReaderRepo.DEFAULT_ENDIAN

    private var _used: Long = 0

    override var endian: ReaderRepo.Endian
        get() = _endian
        set(value) {
            _endian = value
        }

    override val used: Long
        get() = _used

    override val byte: Byte
        get() = read(1).readByte()

    override val uByte: UByte
        get() = read(1).readUByte()

    override val short: Short
        get() = read(2).let {
            if (endian == ReaderRepo.Endian.Big) it.readShortBE()
            else it.readShortLE()
        }

    override val uShort: UShort
        get() = read(2).let {
            if (endian == ReaderRepo.Endian.Big) it.readUShortBE()
            else it.readUShortLE()
        }

    override val int: Int
        get() = read(4).let {
            if (endian == ReaderRepo.Endian.Big) it.readIntBE()
            else it.readIntLE()
        }

    override val uInt: UInt
        get() = read(4).let {
            if (endian == ReaderRepo.Endian.Big) it.readUIntBE()
            else it.readUIntLE()
        }

    override val long: Long
        get() = read(8).let {
            if (endian == ReaderRepo.Endian.Big) it.readLongBE()
            else it.readLongLE()
        }

    override val uLong: ULong
        get() = read(8).let {
            if (endian == ReaderRepo.Endian.Big) it.readULongBE()
            else it.readULongLE()
        }

    override fun read(length: Int): ByteArray {
        return ByteArray(length).also { this.read(it, 0, length) }
    }

    override fun read(bytes: ByteArray, offset: Int, length: Int) {
        var readLength = 0
        while (input.available() > 0 && length > readLength)
            readLength += input.read(
                bytes,
                offset + readLength,
                length - readLength
            )
        _used += readLength
    }

    override fun skip(length: Long) {
        var readLength: Long = 0
        while (input.available() > 0 && length > readLength)
            readLength += input.skip(length - readLength)
        _used += readLength
    }

    override fun close() {
        input.close()
    }
}

fun InputStream.readerRepo(): ReaderRepo {
    return StreamReaderRepoImpl(this)
}
