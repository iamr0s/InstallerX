package com.rosan.installer.data.res.model.entity.error

import com.rosan.installer.data.common.model.entity.ErrorEntity
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader
import kotlinx.serialization.Serializable

@Serializable
class ChunkTypeUseError(val types: List<ChunkHeader.Type>, val useType: ChunkHeader.Type) :
    ErrorEntity(
        "requires '${
            types.joinToString {
                it.name
            }
        }', but uses '${useType.name}'"
    ) {
    companion object {
        fun check(type: ChunkHeader.Type, useType: ChunkHeader.Type) {
//            if (type != useType) throw ChunkTypeUseError(listOf(type), useType)
        }

        fun check(types: List<ChunkHeader.Type>, useType: ChunkHeader.Type) {
//            if (!types.contains(useType)) throw ChunkTypeUseError(types, useType)
        }
    }
}