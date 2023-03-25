package com.rosan.installer.data.res.model.impl.res.axml

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.impl.res.axml.header.XMLTreeNodeHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader

class XMLElement(
    val lineNumber: Int,
    val comment: Int,
    val uri: Int,
    val name: Int,
    val children: List<XMLElement>
) {
    companion object {
        fun build(repo: ReaderRepo, startOrNull: XMLStartElement? = null): XMLElement {
            val start = startOrNull ?: XMLStartElement.build(repo)
            val reader = repo.readerRepo()
            val children = mutableListOf<XMLElement>()
            while (true) {
                val chunkHeader = ChunkHeader.build(reader)
                when (chunkHeader.type) {
                    ChunkHeader.Type.XML_END_ELEMENT -> {
                        XMLEndElement.build(reader, XMLTreeNodeHeader.build(reader, chunkHeader))
                        break
                    }
                    ChunkHeader.Type.XML_START_ELEMENT -> {
                        this.build(
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
            return XMLElement(
                start.lineNumber,
                start.comment,
                start.uri,
                start.name,
                children
            )
        }
    }
}