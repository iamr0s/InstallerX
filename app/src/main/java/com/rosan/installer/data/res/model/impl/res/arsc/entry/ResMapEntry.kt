package com.rosan.installer.data.res.model.impl.res.arsc.entry

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.impl.res.ResEntry
import com.rosan.installer.data.res.model.impl.res.Value
import com.rosan.installer.data.res.model.impl.res.header.EntryHeader

data class ResMapEntry(
    override val key: Int,
    val parent: Int,
    val valueMap: Map<Int, Value>
) : ResEntry {
    companion object {
        fun build(repo: ReaderRepo, headerOrNull: EntryHeader? = null): ResMapEntry {
            val header = headerOrNull ?: EntryHeader.build(repo)
            val reader = repo.readerRepo()
            val parent = reader.int
            val count = reader.int
            val valueMap = mutableMapOf<Int, Value>()
            for (i in 0 until count) {
                val name = reader.int
                valueMap[name] = Value.build(reader)
            }
            return ResMapEntry(
                header.key,
                parent,
                valueMap
            )
        }
    }
}