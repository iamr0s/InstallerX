package com.rosan.installer.data.app.model.entity.error

import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.common.model.entity.ErrorEntity
import kotlinx.serialization.Serializable

@Serializable
class DataEntityNotSupportError(val data: DataEntity) : ErrorEntity("'$data' is not supported") {
}