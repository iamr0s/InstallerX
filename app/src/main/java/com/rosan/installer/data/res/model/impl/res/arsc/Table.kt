package com.rosan.installer.data.res.model.impl.res.arsc

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.impl.res.StringPool
import com.rosan.installer.data.res.model.impl.res.arsc.header.TableHeader
import com.rosan.installer.data.res.model.impl.res.arsc.header.TablePackageHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader
import com.rosan.installer.data.res.model.impl.res.header.StringPoolHeader

data class Table(
    val stringPool: StringPool,
    val packages: List<TablePackage>
) {
    companion object {
        fun build(repo: ReaderRepo, headerOrNull: TableHeader? = null): Table {
            val header = headerOrNull ?: TableHeader.build(repo, ChunkHeader.build(repo))
            val reader = repo.readerRepo()
            val stringPool =
                StringPool.build(reader, StringPoolHeader.build(reader, ChunkHeader.build(reader)))
            val packages = mutableListOf<TablePackage>()
            for (i in 0 until header.packageCount) {
                TablePackage.build(
                    reader,
                    TablePackageHeader.build(reader, ChunkHeader.build(reader))
                ).also {
                    packages.add(it)
                }
            }
            if (header.chunkSize > header.length + reader.used) reader.skip(header.chunkSize - header.length - reader.used)
            return Table(
                stringPool,
                packages
            )
        }
    }
}