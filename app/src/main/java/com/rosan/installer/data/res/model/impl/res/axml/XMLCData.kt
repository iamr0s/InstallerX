package com.rosan.installer.data.res.model.impl.res.axml

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.error.ChunkTypeUseError
import com.rosan.installer.data.res.model.impl.res.Value
import com.rosan.installer.data.res.model.impl.res.axml.header.XMLTreeNodeHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader

data class XMLCData(
    override val lineNumber: Int,
    override val comment: Int,
    val data: Int,
    val typedValue: Value
) : XMLChunk(lineNumber, comment) {
    companion object {
        fun build(repo: ReaderRepo, headerOrNull: XMLTreeNodeHeader? = null): XMLCData {
            val header = headerOrNull ?: XMLTreeNodeHeader.build(repo)
            ChunkTypeUseError.check(ChunkHeader.Type.XML_END_ELEMENT, header.type)
            val reader = repo.readerRepo()

            val data = reader.int
            val typedValue = Value.build(reader)

            if (header.chunkSize > header.length + reader.used) reader.skip(header.chunkSize - header.length - reader.used)
            return XMLCData(
                header.lineNumber,
                header.comment,
                data,
                typedValue
            )
        }
    }
}