package com.rosan.installer.data.app.model.entity.error

class InstallFailedMediaUnavailableException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}