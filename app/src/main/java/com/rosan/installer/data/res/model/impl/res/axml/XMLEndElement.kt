package com.rosan.installer.data.res.model.impl.res.axml

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.error.ChunkTypeUseError
import com.rosan.installer.data.res.model.impl.res.axml.header.XMLTreeNodeHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader

data class XMLEndElement(
    override val lineNumber: Int,
    override val comment: Int,
    val uri: Int,
    val name: Int
) : XMLChunk(lineNumber, comment) {
    companion object {
        fun build(repo: ReaderRepo, headerOrNull: XMLTreeNodeHeader? = null): XMLEndElement {
            val header = headerOrNull ?: XMLTreeNodeHeader.build(repo)
            ChunkTypeUseError.check(ChunkHeader.Type.XML_END_ELEMENT, header.type)
            val reader = repo.readerRepo()

            val uri = reader.int
            val name = reader.int

            if (header.chunkSize > header.length + reader.used) reader.skip(header.chunkSize - header.length - reader.used)
            return XMLEndElement(
                header.lineNumber,
                header.comment,
                uri,
                name
            )
        }
    }
}