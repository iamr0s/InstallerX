package com.rosan.installer.data.app.model.entity.error

class InstallFailedReplaceCouldntDeleteException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}