package com.rosan.installer.data.common.model.entity

import kotlinx.serialization.Serializable

@Serializable
open class ErrorEntity(override val message: String) : Throwable(message = message) {
}