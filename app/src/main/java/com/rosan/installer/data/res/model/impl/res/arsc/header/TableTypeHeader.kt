package com.rosan.installer.data.res.model.impl.res.arsc.header

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.error.ChunkTypeUseError
import com.rosan.installer.data.res.model.impl.res.arsc.TableConfig
import com.rosan.installer.data.res.model.impl.res.header.BaseHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader

data class TableTypeHeader(
    val headerSize: Int,
    override val chunkSize: Int,
    val id: Int,
    val flags: Flags,
    val entryCount: Int,
    val entriesOffset: Int,
    val config: TableConfig
) : BaseHeader {
    override val length = headerSize

    constructor(
        chunkHeader: ChunkHeader,
        id: Int,
        flags: Flags,
        entryCount: Int,
        entriesOffset: Int,
        config: TableConfig
    ) : this(
        chunkHeader.headerSize,
        chunkHeader.chunkSize,
        id,
        flags,
        entryCount,
        entriesOffset,
        config
    )

    data class Flags(val value: UByte) {
        val isSparse = value and 0x01u != 0x0u.toUByte()

        companion object {
            fun build(repo: ReaderRepo): Flags {
                return Flags(repo.uByte)
            }
        }
    }

    companion object {
        fun build(repo: ReaderRepo, chunkHeader: ChunkHeader): TableTypeHeader {
            ChunkTypeUseError.check(ChunkHeader.Type.TABLE_TYPE, chunkHeader.type)
            val reader = repo.readerRepo()

            val id = reader.uByte.toInt()
            val flags = Flags.build(reader)
            reader.skip(2)
            val entryCount = reader.int
            val entriesOffset = reader.int
            val config = TableConfig.build(reader)

            if (chunkHeader.headerSize > chunkHeader.length + reader.used) reader.skip(chunkHeader.headerSize - chunkHeader.length - reader.used)
            return TableTypeHeader(
                chunkHeader,
                id,
                flags,
                entryCount,
                entriesOffset,
                config
            )
        }
    }
}