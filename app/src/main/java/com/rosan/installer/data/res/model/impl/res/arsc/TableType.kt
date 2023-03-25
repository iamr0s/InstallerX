package com.rosan.installer.data.res.model.impl.res.arsc

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.impl.res.ResEntry
import com.rosan.installer.data.res.model.impl.res.arsc.entry.ResMapEntry
import com.rosan.installer.data.res.model.impl.res.header.EntryHeader
import com.rosan.installer.data.res.model.impl.res.arsc.header.TableTypeHeader
import com.rosan.installer.data.res.model.impl.res.entry.ResValueEntry

data class TableType(
    val id: Int,
    val config: TableConfig,
    val entryMap: Map<Int, ResEntry>
) {
    companion object {
        fun build(repo: ReaderRepo, header: TableTypeHeader): TableType {
            val reader = repo.readerRepo()

            val offsetMap = mutableMapOf<Int, Int>()
            for (i in 0 until header.entryCount)
                if (header.flags.isSparse)
                    offsetMap[reader.uShort.toInt()] = (reader.uShort * 4u).toInt()
                else offsetMap[i] = reader.int

            if (header.entriesOffset > header.length + reader.used) reader.skip(header.entriesOffset - header.length - reader.used)

            val entryMap = buildEntryMap(reader, offsetMap)

            if (header.chunkSize > header.length + reader.used) reader.skip(header.chunkSize - header.length - reader.used)
            return TableType(
                header.id,
                header.config,
                entryMap
            )
        }

        private fun buildEntryMap(
            repo: ReaderRepo,
            offsetMap: Map<Int, Int>
        ): MutableMap<Int, ResEntry> {
            val reader = repo.readerRepo()

            val entryMap = mutableMapOf<Int, ResEntry>()

            // sort it by offset, little offset first
            offsetMap.toSortedMap(compareBy {
                offsetMap[it]
            }).forEach {
                val i = it.key
                val offset = it.value
                if (offset == -1) return@forEach
                if (offset > reader.used) reader.skip(offset - reader.used)
                val header = EntryHeader.build(reader)
                if (header.flags.isComplex) ResMapEntry.build(reader, header)
                else entryMap[i] = ResValueEntry.build(reader, header)
            }
            return entryMap
        }
    }
}