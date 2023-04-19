package com.rosan.installer.data.app.model.entity.error

class InstallFailedUpdateIncompatibleException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}