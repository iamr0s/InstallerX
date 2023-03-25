package com.rosan.installer.data.res.model.impl.res

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.ValueEntity

data class Value(
    val type: ValueEntity.Type,
    val data: Int
) {
    companion object {
        fun build(repo: ReaderRepo): Value {
            val reader = repo.readerRepo()
            val size = reader.uShort.toInt()
            reader.skip(1)
            val type = reader.uByte
            val data = reader.int
            if (size > reader.used) reader.skip(size - reader.used)
            return Value(
                ValueEntity.Type.build(type),
                data
            )
        }
    }
}
