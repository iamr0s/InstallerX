package com.rosan.installer.data.app.model.exception

class InstallFailedContainerErrorException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}