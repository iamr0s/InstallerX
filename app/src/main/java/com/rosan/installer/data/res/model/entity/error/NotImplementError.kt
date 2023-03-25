package com.rosan.installer.data.res.model.entity.error

import com.rosan.installer.data.common.model.entity.ErrorEntity
import kotlinx.serialization.Serializable

@Serializable
class NotImplementError(val name: String? = null) : ErrorEntity(
    if (name == null)
        "An operation is not implemented."
    else
        "An operation is not implemented: $name"
)