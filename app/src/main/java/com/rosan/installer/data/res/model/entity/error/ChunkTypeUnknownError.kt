package com.rosan.installer.data.res.model.entity.error

import com.rosan.installer.data.common.model.entity.ErrorEntity
import kotlinx.serialization.Serializable

@Serializable
class ChunkTypeUnknownError(val type: UShort) :
    ErrorEntity("'${String.format("0x%04X", type.toInt())}' is not support") {
}