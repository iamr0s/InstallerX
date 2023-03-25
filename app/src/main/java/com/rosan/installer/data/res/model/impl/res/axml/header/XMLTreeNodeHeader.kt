package com.rosan.installer.data.res.model.impl.res.axml.header

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.impl.res.header.BaseHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader

class XMLTreeNodeHeader(
    val type: ChunkHeader.Type,
    headerSize: Int,
    override val chunkSize: Int,
    val lineNumber: Int,
    val comment: Int,
) : BaseHeader {
    override val length = headerSize

    constructor(
        chunkHeader: ChunkHeader,
        lineNumber: Int,
        comment: Int,
    ) : this(
        chunkHeader.type,
        chunkHeader.headerSize,
        chunkHeader.chunkSize,
        lineNumber,
        comment
    )

    companion object {
        fun build(repo: ReaderRepo, chunkHeaderOrNull: ChunkHeader? = null): XMLTreeNodeHeader {
            val chunkHeader = chunkHeaderOrNull ?: ChunkHeader.build(repo)
            val reader = repo.readerRepo()
            val lineNumber = reader.int
            val comment = reader.int
            if (chunkHeader.headerSize > chunkHeader.length + reader.used) reader.skip(chunkHeader.headerSize - chunkHeader.length - reader.used)
            return XMLTreeNodeHeader(
                chunkHeader,
                lineNumber,
                comment
            )
        }
    }
}
