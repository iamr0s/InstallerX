package com.rosan.installer.data.res.model.impl.res.axml.header

import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.error.ChunkTypeUseError
import com.rosan.installer.data.res.model.impl.res.header.BaseHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader

data class ResourceMapHeader(
    val type: ChunkHeader.Type,
    val headerSize: Int,
    override val chunkSize: Int,
    val resourceCount: Int
) : BaseHeader {
    override val length = headerSize

    constructor(
        chunkHeader: ChunkHeader,
        resourceCount: Int,
    ) : this(
        chunkHeader.type,
        chunkHeader.headerSize,
        chunkHeader.chunkSize,
        resourceCount
    )

    companion object {
        fun build(repo: ReaderRepo, chunkHeaderOrNull: ChunkHeader? = null): ResourceMapHeader {
            val chunkHeader = chunkHeaderOrNull ?: ChunkHeader.build(repo)
            ChunkTypeUseError.check(ChunkHeader.Type.XML_RESOURCE_MAP, chunkHeader.type)

            val resourceCount = (chunkHeader.chunkSize - chunkHeader.length) / 4;

            if (chunkHeader.headerSize > chunkHeader.length) repo.skip(chunkHeader.chunkSize - chunkHeader.length)
            return ResourceMapHeader(
                chunkHeader,
                resourceCount
            )
        }
    }
}