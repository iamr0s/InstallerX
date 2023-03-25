package com.rosan.installer.data.res.model.impl.res.header

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.error.ChunkTypeUseError

data class StringPoolHeader(
    val headerSize: Int,
    override val chunkSize: Int,
    val stringCount: Int,
    val styleCount: Int,
    val flags: Flags,
    val stringsStart: Int,
    val stylesStart: Int,
) : BaseHeader {
    override val length = headerSize

    constructor(
        chunkHeader: ChunkHeader,
        stringCount: Int,
        styleCount: Int,
        flags: Flags,
        stringsStart: Int,
        stylesStart: Int,
    ) : this(
        chunkHeader.headerSize,
        chunkHeader.chunkSize,
        stringCount,
        styleCount,
        flags,
        stringsStart,
        stylesStart
    )

    class Flags(val value: UInt) {
        val isSorted = value and 1u != 0u

        val isUTF8: Boolean = value and (1u shl 8) != 0u
    }

    companion object {
        fun build(repo: ReaderRepo, chunkHeader: ChunkHeader): StringPoolHeader {
            ChunkTypeUseError.check(ChunkHeader.Type.STRING_POOL, chunkHeader.type)
            val reader = repo.readerRepo()
            val ret = StringPoolHeader(
                chunkHeader,
                reader.int,
                reader.int,
                Flags(reader.uInt),
                reader.int,
                reader.int
            )
            if (chunkHeader.headerSize > chunkHeader.length + reader.used) reader.skip(chunkHeader.headerSize - chunkHeader.length - reader.used)
            return ret
        }
    }
}