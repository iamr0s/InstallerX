package com.rosan.installer.data.res.model.impl.res.axml.header

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.error.ChunkTypeUseError
import com.rosan.installer.data.res.model.impl.res.header.BaseHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader

data class XMLTreeHeader(
    val type: ChunkHeader.Type,
    val headerSize: Int,
    override val chunkSize: Int,
) : BaseHeader {
    override val length = headerSize

    constructor(
        chunkHeader: ChunkHeader,
    ) : this(
        chunkHeader.type,
        chunkHeader.headerSize,
        chunkHeader.chunkSize
    )

    companion object {
        fun build(repo: ReaderRepo, chunkHeaderOrNull: ChunkHeader? = null): XMLTreeHeader {
            val chunkHeader = chunkHeaderOrNull ?: ChunkHeader.build(repo)
            ChunkTypeUseError.check(ChunkHeader.Type.XML, chunkHeader.type)
            val reader = repo.readerRepo()
            if (chunkHeader.headerSize > chunkHeader.length + reader.used) reader.skip(chunkHeader.length + reader.used)
            return XMLTreeHeader(
                chunkHeader
            )
        }
    }
}