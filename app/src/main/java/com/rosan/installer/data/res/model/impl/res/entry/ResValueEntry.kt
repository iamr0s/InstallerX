package com.rosan.installer.data.res.model.impl.res.entry

import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.impl.res.ResEntry
import com.rosan.installer.data.res.model.impl.res.Value
import com.rosan.installer.data.res.model.impl.res.header.EntryHeader

data class ResValueEntry(
    override val key: Int,
    val value: Value
) : ResEntry {
    companion object {
        fun build(repo: ReaderRepo, headerOrNull: EntryHeader? = null): ResValueEntry {
            val header = headerOrNull ?: EntryHeader.build(repo)
            return ResValueEntry(
                header.key,
                Value.build(repo)
            )
        }
    }
}