package com.rosan.installer.data.app.model.entity.error

class InstallFailedContainerErrorException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}