package com.rosan.installer.data.res.model.impl.res.arsc.header

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.error.ChunkTypeUseError
import com.rosan.installer.data.res.model.impl.res.header.BaseHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader

data class TableTypeSpecHeader(
    val headerSize: Int,
    override val chunkSize: Int,
    val id: Int,
    val entryCount: Int,
) : BaseHeader {
    override val length = headerSize

    constructor(
        chunkHeader: ChunkHeader,
        id: Int,
        entryCount: Int,
    ) : this(
        chunkHeader.headerSize,
        chunkHeader.chunkSize,
        id,
        entryCount
    )

    companion object {
        fun build(repo: ReaderRepo, chunkHeader: ChunkHeader): TableTypeSpecHeader {
            ChunkTypeUseError.check(ChunkHeader.Type.TABLE_TYPE_SPEC, chunkHeader.type)
            val reader = repo.readerRepo()
            val id = reader.uByte.toInt()

            // Must be 0
            // 保留字段
            reader.skip(3)

            val entryCount = reader.int
            if (chunkHeader.headerSize > chunkHeader.length + reader.used) reader.skip(chunkHeader.chunkSize - chunkHeader.length - reader.used)
            return TableTypeSpecHeader(chunkHeader, id, entryCount)
        }
    }
}