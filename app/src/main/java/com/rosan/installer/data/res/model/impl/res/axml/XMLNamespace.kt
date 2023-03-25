package com.rosan.installer.data.res.model.impl.res.axml

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.impl.res.axml.header.XMLTreeNodeHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader

class XMLNamespace(
    val lineNumber: Int,
    val comment: Int,
    val prefix: Int,
    val uri: Int,
    val children: List<XMLElement>
) {
    companion object {
        fun build(repo: ReaderRepo, startOrNull: XMLStartNamespace? = null): XMLNamespace {
            val start = startOrNull ?: XMLStartNamespace.build(repo)
            val reader = repo.readerRepo()
            val children = mutableListOf<XMLElement>()
            while (true) {
                val chunkHeader = ChunkHeader.build(reader)
                when (chunkHeader.type) {
                    ChunkHeader.Type.XML_END_NAMESPACE -> {
                        XMLEndNamespace.build(reader, XMLTreeNodeHeader.build(reader, chunkHeader))
                        break
                    }
                    ChunkHeader.Type.XML_START_ELEMENT -> {
                        XMLElement.build(
                            reader,
                            XMLStartElement.build(
                                reader,
                                XMLTreeNodeHeader.build(reader, chunkHeader)
                            )
                        ).also {
                            children.add(it)
                        }
                    }
                    else -> {
                        if (chunkHeader.chunkSize > chunkHeader.length) reader.skip(chunkHeader.chunkSize - chunkHeader.length)
                    }
                }
            }
            return XMLNamespace(
                start.lineNumber,
                start.comment,
                start.prefix,
                start.uri,
                children
            )
        }
    }
}