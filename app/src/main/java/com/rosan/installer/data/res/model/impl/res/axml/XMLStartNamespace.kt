package com.rosan.installer.data.res.model.impl.res.axml

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.error.ChunkTypeUseError
import com.rosan.installer.data.res.model.impl.res.axml.header.XMLTreeNodeHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader

data class XMLStartNamespace(
    override val lineNumber: Int,
    override val comment: Int,
    val prefix: Int,
    val uri: Int
) : XMLChunk(lineNumber, comment) {
    companion object {
        fun build(repo: ReaderRepo, headerOrNull: XMLTreeNodeHeader? = null): XMLStartNamespace {
            val header = headerOrNull ?: XMLTreeNodeHeader.build(repo)
            ChunkTypeUseError.check(ChunkHeader.Type.XML_START_NAMESPACE, header.type)
            val reader = repo.readerRepo()

            val prefix = reader.int
            val uri = reader.int

            if (header.chunkSize > header.length + reader.used) reader.skip(header.chunkSize - header.length - reader.used)
            return XMLStartNamespace(
                header.lineNumber,
                header.comment,
                prefix,
                uri
            )
        }
    }
}