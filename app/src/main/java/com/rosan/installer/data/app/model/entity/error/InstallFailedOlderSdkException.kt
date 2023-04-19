package com.rosan.installer.data.app.model.entity.error

class InstallFailedOlderSdkException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}