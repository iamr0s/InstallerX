package com.rosan.installer.data.io.model.entity.error

import com.rosan.installer.data.common.model.entity.ErrorEntity
import kotlinx.serialization.Serializable

@Serializable
class ByteArrayOffsetError(
    val size: Int,
    val offset: Int,
    val length: Int
) : ErrorEntity("Out of range. 'size' is $size. 'offset' is $offset. 'length' is $length") {
}