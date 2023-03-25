package com.rosan.installer.data.res.model.impl.res.axml

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.error.ChunkTypeUseError
import com.rosan.installer.data.res.model.impl.res.axml.header.XMLTreeNodeHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader

data class XMLStartElement(
    override val lineNumber: Int,
    override val comment: Int,
    val uri: Int,
    val name: Int,
    val idIndex: Int,
    val classIndex: Int,
    val styleIndex: Int,
    val attributes: List<XMLAttribute>
) : XMLChunk(lineNumber, comment) {
    val isNoneId = idIndex == 0

    val isNoneClass = classIndex == 0

    val isNoneStyle = styleIndex == 0

    companion object {
        fun build(repo: ReaderRepo, headerOrNull: XMLTreeNodeHeader? = null): XMLStartElement {
            val header = headerOrNull ?: XMLTreeNodeHeader.build(repo)
            ChunkTypeUseError.check(ChunkHeader.Type.XML_START_ELEMENT, header.type)
            val reader = repo.readerRepo()

            val uri = reader.int
            val name = reader.int
            val attributeStart = reader.uShort.toInt()
            val attributeSize = reader.uShort.toInt()
            val attributeCount = reader.uShort.toInt()
            val idIndex = reader.uShort.toInt()
            val classIndex = reader.uShort.toInt()
            val styleIndex = reader.uShort.toInt()
            val attributes = mutableListOf<XMLAttribute>()

            if (attributeStart > reader.used) reader.skip(attributeStart - reader.used)

            for (i in 0 until attributeCount) {
                XMLAttribute.build(reader).also {
                    attributes.add(it)
                }
            }

            if (header.chunkSize > header.length + reader.used) reader.skip(header.chunkSize - header.length - reader.used)
            return XMLStartElement(
                header.lineNumber,
                header.comment,
                uri,
                name,
                idIndex,
                classIndex,
                styleIndex,
                attributes
            )
        }
    }
}