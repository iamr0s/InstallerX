package com.rosan.installer.data.io.util

import com.rosan.installer.data.io.model.entity.error.ByteArrayOffsetError
import java.nio.charset.Charset

fun ByteArray.readByte(offset: Int = 0): Byte {
    this.throwOffsetError(offset, 1)
    return this[offset]
}

fun ByteArray.readUByte(offset: Int = 0): UByte {
    this.throwOffsetError(offset, 1)
    return this[offset].toUByte()
}

fun ByteArray.readShortBE(offset: Int = 0): Short {
    this.throwOffsetError(offset, 2)
    return ((this[offset].toInt() shl 8) or
            (this[offset + 1].toInt() and 0xFF)).toShort()
}

fun ByteArray.readUShortBE(offset: Int = 0): UShort {
    return this.readShortBE(offset).toUShort()
}

fun ByteArray.readShortLE(offset: Int = 0): Short {
    this.throwOffsetError(offset, 2)
    return ((this[offset + 1].toInt() shl 8) or
            (this[offset].toInt() and 0xFF)).toShort()
}

fun ByteArray.readUShortLE(offset: Int = 0): UShort {
    return this.readShortLE(offset).toUShort()
}

fun ByteArray.readIntBE(offset: Int = 0): Int {
    this.throwOffsetError(offset, 4)
    return (this[offset].toInt()) shl 24 or
            ((this[offset + 1].toInt() and 0xFF) shl 16) or
            ((this[offset + 2].toInt() and 0xFF) shl 8) or
            (this[offset + 3].toInt() and 0xFF)
}

fun ByteArray.readUIntBE(offset: Int = 0): UInt {
    return this.readIntBE(offset).toUInt()
}

fun ByteArray.readIntLE(offset: Int = 0): Int {
    this.throwOffsetError(offset, 4)
    return (this[offset + 3].toInt()) shl 24 or
            ((this[offset + 2].toInt() and 0xFF) shl 16) or
            ((this[offset + 1].toInt() and 0xFF) shl 8) or
            (this[offset].toInt() and 0xFF)
}

fun ByteArray.readUIntLE(offset: Int = 0): UInt {
    return this.readIntLE(offset).toUInt()
}

fun ByteArray.readLongBE(offset: Int = 0): Long {
    this.throwOffsetError(offset, 8)
    return (this[offset].toLong()) shl 56 or
            ((this[offset + 1].toLong() and 0xFF) shl 48) or
            ((this[offset + 2].toLong() and 0xFF) shl 40) or
            ((this[offset + 3].toLong() and 0xFF) shl 32) or
            ((this[offset + 4].toLong() and 0xFF) shl 24) or
            ((this[offset + 5].toLong() and 0xFF) shl 16) or
            ((this[offset + 6].toLong() and 0xFF) shl 8) or
            (this[offset + 7].toLong() and 0xFF)
}

fun ByteArray.readULongBE(offset: Int = 0): ULong {
    return this.readLongBE(offset).toULong()
}

fun ByteArray.readLongLE(offset: Int = 0): Long {
    this.throwOffsetError(offset, 8)
    return (this[offset + 7].toLong()) shl 56 or
            ((this[offset + 6].toLong() and 0xFF) shl 48) or
            ((this[offset + 5].toLong() and 0xFF) shl 40) or
            ((this[offset + 4].toLong() and 0xFF) shl 32) or
            ((this[offset + 3].toLong() and 0xFF) shl 24) or
            ((this[offset + 2].toLong() and 0xFF) shl 16) or
            ((this[offset + 1].toLong() and 0xFF) shl 8) or
            (this[offset].toLong() and 0xFF)
}

fun ByteArray.readULongLE(offset: Int = 0): ULong {
    return this.readLongLE(offset).toULong()
}

fun ByteArray.readFloatBE(offset: Int = 0): Float {
    return java.lang.Float.intBitsToFloat(this.readIntBE(offset))
}

fun ByteArray.readFloatLE(offset: Int = 0): Float {
    return java.lang.Float.intBitsToFloat(this.readIntLE(offset))
}

fun ByteArray.readDoubleBE(offset: Int = 0): Double {
    return java.lang.Double.longBitsToDouble(this.readLongBE(offset))
}

fun ByteArray.readDoubleLE(offset: Int = 0): Double {
    return java.lang.Double.longBitsToDouble(this.readLongLE(offset))
}

fun ByteArray.readString(
    offset: Int = 0,
    length: Int = this.size - offset,
    charset: Charset = Charset.defaultCharset()
): String {
    this.throwOffsetError(offset, length)
    return String(this, offset, length, charset)/*.substringBefore(Char(0))*/
}

fun ByteArray.throwOffsetError(offset: Int, length: Int) {
    if (length > this.size - offset) throw ByteArrayOffsetError(this.size, offset, length)
}
