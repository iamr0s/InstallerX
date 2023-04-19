package com.rosan.installer.data.app.model.entity.error

class InstallFailedAlreadyExistsException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}