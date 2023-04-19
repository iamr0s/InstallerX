package com.rosan.installer.data.app.model.entity.error

class InstallFailedInvalidInstallLocationException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}