package com.rosan.installer.data.installer.model.entity.error

import android.net.Uri
import com.rosan.installer.data.common.model.entity.ErrorEntity

data class ResolveError(
    val action: String?,
    val uris: List<Uri>,
) : ErrorEntity(
    "action: $action, uri: $uris"
) {
}