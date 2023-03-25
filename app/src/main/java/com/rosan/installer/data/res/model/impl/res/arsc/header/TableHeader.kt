package com.rosan.installer.data.res.model.impl.res.arsc.header

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.error.ChunkTypeUseError
import com.rosan.installer.data.res.model.impl.res.header.BaseHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader

data class TableHeader(
    val headerSize: Int,
    override val chunkSize: Int,
    val packageCount: Int
) : BaseHeader {
    override val length = headerSize

    constructor(
        chunkHeader: ChunkHeader,
        packageCount: Int
    ) : this(
        chunkHeader.headerSize,
        chunkHeader.chunkSize,
        packageCount
    )

    companion object {
        fun build(repo: ReaderRepo, chunkHeader: ChunkHeader): TableHeader {
            ChunkTypeUseError.check(ChunkHeader.Type.TABLE, chunkHeader.type)
            val reader = repo.readerRepo()
            val packageCount = reader.int
            if (chunkHeader.headerSize > chunkHeader.length + reader.used) reader.skip(chunkHeader.headerSize - chunkHeader.length - reader.used)
            return TableHeader(
                chunkHeader,
                packageCount
            )
        }
    }
}
