package com.rosan.installer.data.app.model.exception

class InstallFailedInsufficientStorageException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}