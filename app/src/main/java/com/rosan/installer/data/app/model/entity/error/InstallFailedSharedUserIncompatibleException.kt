package com.rosan.installer.data.app.model.entity.error

class InstallFailedSharedUserIncompatibleException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}