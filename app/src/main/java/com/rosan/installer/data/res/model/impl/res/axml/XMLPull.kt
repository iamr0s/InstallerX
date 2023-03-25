package com.rosan.installer.data.res.model.impl.res.axml

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.impl.res.StringPool
import com.rosan.installer.data.res.model.impl.res.axml.header.ResourceMapHeader
import com.rosan.installer.data.res.model.impl.res.axml.header.XMLTreeHeader
import com.rosan.installer.data.res.model.impl.res.axml.header.XMLTreeNodeHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader
import com.rosan.installer.data.res.model.impl.res.header.StringPoolHeader

class XMLPull(
    private val reader: ReaderRepo,
    headerOrNull: XMLTreeHeader? = null
) {
    private val header = headerOrNull ?: XMLTreeHeader.build(reader)

    private var _stringPool = StringPool.empty

    val stringPool: StringPool
        get() = _stringPool

    var _resourceMap = listOf<Int>()

    val resourceMap: List<Int>
        get() = _resourceMap

    fun next(): XMLChunk? {
        var chunk: XMLChunk? = null
        while (header.chunkSize > reader.used && chunk == null) {
            val chunkHeader = ChunkHeader.build(reader)
            when (chunkHeader.type) {
                ChunkHeader.Type.STRING_POOL -> _stringPool =
                    StringPool.build(reader, StringPoolHeader.build(reader, chunkHeader))
                ChunkHeader.Type.XML_RESOURCE_MAP -> _resourceMap = buildResourceMap(
                    reader,
                    ResourceMapHeader.build(reader, chunkHeader)
                )
                ChunkHeader.Type.XML_START_NAMESPACE -> chunk = XMLStartNamespace.build(
                    reader,
                    XMLTreeNodeHeader.build(reader, chunkHeader)
                )
                ChunkHeader.Type.XML_START_ELEMENT -> chunk = XMLStartElement.build(
                    reader,
                    XMLTreeNodeHeader.build(reader, chunkHeader)
                )
                ChunkHeader.Type.XML_END_ELEMENT -> chunk = XMLEndElement.build(
                    reader,
                    XMLTreeNodeHeader.build(reader, chunkHeader)
                )
                ChunkHeader.Type.XML_END_NAMESPACE -> chunk = XMLEndNamespace.build(
                    reader,
                    XMLTreeNodeHeader.build(reader, chunkHeader)
                )
                else -> {
                    // skip unknown chunk
                    if (chunkHeader.chunkSize > chunkHeader.length) reader.skip(chunkHeader.chunkSize - chunkHeader.length)
                }
            }
        }
        return chunk
    }

    private fun buildResourceMap(
        repo: ReaderRepo,
        headerOrNull: ResourceMapHeader? = null
    ): List<Int> {
        val header = headerOrNull ?: ResourceMapHeader.build(repo)
        val reader = repo.readerRepo()
        val resourceMap = mutableListOf<Int>()

        for (i in 0 until header.resourceCount) {
            resourceMap.add(reader.int)
        }

        if (header.chunkSize > header.length + reader.used) reader.skip(header.chunkSize - header.length - reader.used)
        return resourceMap
    }
}