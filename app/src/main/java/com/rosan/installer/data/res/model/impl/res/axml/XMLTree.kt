package com.rosan.installer.data.res.model.impl.res.axml

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.impl.res.StringPool
import com.rosan.installer.data.res.model.impl.res.axml.header.ResourceMapHeader
import com.rosan.installer.data.res.model.impl.res.axml.header.XMLTreeHeader
import com.rosan.installer.data.res.model.impl.res.axml.header.XMLTreeNodeHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader
import com.rosan.installer.data.res.model.impl.res.header.StringPoolHeader

class XMLTree(
    val stringPool: StringPool,
    val resourceMap: List<Int>,
    val namespaces: List<XMLNamespace>
) {
    companion object {
        fun build(repo: ReaderRepo, headerOrNull: XMLTreeHeader? = null): XMLTree {
            val header = headerOrNull ?: XMLTreeHeader.build(repo)
            val reader = repo.readerRepo()
            var stringPool = StringPool.empty
            var resourceMap = listOf<Int>()
            val namespaces = mutableListOf<XMLNamespace>()

            // 直到读完当前Chunk
            while (header.length + reader.used < header.chunkSize) {
                val chunkHeader = ChunkHeader.build(reader)
                when (chunkHeader.type) {
                    ChunkHeader.Type.STRING_POOL -> {
                        stringPool =
                            StringPool.build(reader, StringPoolHeader.build(reader, chunkHeader))
                    }
                    ChunkHeader.Type.XML_RESOURCE_MAP -> {
                        resourceMap =
                            buildResourceMap(reader, ResourceMapHeader.build(reader, chunkHeader))
                    }
                    ChunkHeader.Type.XML_START_NAMESPACE -> {
                        XMLNamespace.build(
                            reader, XMLStartNamespace.build(
                                reader,
                                XMLTreeNodeHeader.build(reader, chunkHeader)
                            )
                        ).also {
                            namespaces.add(it)
                        }
                    }
                    else -> {
                        // 忽略未知Chunk
                        if (chunkHeader.chunkSize > chunkHeader.length) reader.skip(chunkHeader.chunkSize - chunkHeader.length)
                    }
                }
            }
            if (header.chunkSize > header.length + reader.used) reader.skip(header.chunkSize - header.length - reader.used)
            return XMLTree(
                stringPool,
                resourceMap,
                namespaces
            )
        }

        fun buildResourceMap(repo: ReaderRepo, headerOrNull: ResourceMapHeader? = null): List<Int> {
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
}