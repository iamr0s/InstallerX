package com.rosan.installer.data.res.model.impl.res.arsc.header

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.io.util.readString
import com.rosan.installer.data.res.model.entity.error.ChunkTypeUseError
import com.rosan.installer.data.res.model.impl.res.header.BaseHeader
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader

data class TablePackageHeader(
    val headerSize: Int,
    override val chunkSize: Int,
    val id: Int,
    val name: String,
    val typeStringsOffset: Int,
    val lastPublicType: Int,
    val keyStringsOffset: Int,
    val lastPublicKey: Int,
    val typeIdOffset: Int
) : BaseHeader {
    override val length = headerSize

    constructor(
        chunkHeader: ChunkHeader,
        id: Int,
        name: String,
        typeStringsOffset: Int,
        lastPublicType: Int,
        keyStringsOffset: Int,
        lastPublicKey: Int,
        typeIdOffset: Int
    ) : this(
        chunkHeader.headerSize,
        chunkHeader.chunkSize,
        id,
        name,
        typeStringsOffset,
        lastPublicType,
        keyStringsOffset,
        lastPublicKey,
        typeIdOffset
    )

    companion object {
        fun build(repo: ReaderRepo, chunkHeader: ChunkHeader): TablePackageHeader {
            ChunkTypeUseError.check(ChunkHeader.Type.TABLE_PACKAGE, chunkHeader.type)
            val reader = repo.readerRepo()
            val ret = TablePackageHeader(
                chunkHeader,
                reader.int,
                reader.read(256)
                    .readString(
                        charset = if (reader.endian == ReaderRepo.Endian.Big)
                            Charsets.UTF_16BE
                        else
                            Charsets.UTF_16LE
                    )
                    .substringBefore(Char(0x0)),
                reader.int,
                reader.int,
                reader.int,
                reader.int,
                reader.int
            )
            if (chunkHeader.headerSize > chunkHeader.length + reader.used) reader.skip(chunkHeader.headerSize - chunkHeader.length - reader.used)
            return ret
        }
    }
}
