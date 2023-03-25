package com.rosan.installer.data.res.model.impl.res.arsc

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.error.ChunkTypeUseError
import com.rosan.installer.data.res.model.impl.res.StringPool
import com.rosan.installer.data.res.model.impl.res.arsc.header.*
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader
import com.rosan.installer.data.res.model.impl.res.header.StringPoolHeader

data class TablePackage(
    val id: Int,
    val name: String,
    val typeStringPool: StringPool,
    val keyStringPool: StringPool,
    val typeSpecMap: Map<Int, TableTypeSpec>,
    val typeMap: Map<Int, List<TableType>>
) {
    companion object {
        fun build(repo: ReaderRepo, header: TablePackageHeader): TablePackage {
            val reader = repo.readerRepo()
            if (header.typeStringsOffset > header.length + reader.used) reader.skip(header.typeStringsOffset - header.length - reader.used)
            val typeStringPool = StringPool.build(
                reader, StringPoolHeader.build(
                    reader,
                    ChunkHeader.build(reader)
                )
            )
            if (header.keyStringsOffset > header.length + reader.used) reader.skip(header.keyStringsOffset - header.length - reader.used)
            val keyStringPool = StringPool.build(
                reader, StringPoolHeader.build(
                    reader,
                    ChunkHeader.build(reader)
                )
            )

            val typeSpecMap = mutableMapOf<Int, TableTypeSpec>()
            val typesMap = mutableMapOf<Int, MutableList<TableType>>()
            while (header.chunkSize > header.length + reader.used) {
                val chunkHeader = ChunkHeader.build(reader)
                when (chunkHeader.type) {
                    ChunkHeader.Type.TABLE_TYPE_SPEC -> {
                        TableTypeSpec.build(reader, TableTypeSpecHeader.build(reader, chunkHeader))
                            .also {
                                typeSpecMap[it.id] = it
                            }
                    }
                    ChunkHeader.Type.TABLE_TYPE -> {
                        TableType.build(reader, TableTypeHeader.build(reader, chunkHeader))
                            .also {
                                val types = typesMap[it.id] ?: mutableListOf()
                                types.add(it)
                                typesMap[it.id] = types
                            }
                    }
                    else -> {
                        reader.skip(chunkHeader.chunkSize - chunkHeader.length)
                    }
                }
            }

            if (header.chunkSize > header.length + reader.used) reader.skip(header.chunkSize - header.length - reader.used)
            return TablePackage(
                header.id,
                header.name,
                typeStringPool,
                keyStringPool,
                typeSpecMap,
                typesMap
            )
        }
    }
}