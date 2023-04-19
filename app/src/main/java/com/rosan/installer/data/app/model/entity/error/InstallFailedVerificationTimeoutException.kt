package com.rosan.installer.data.app.model.entity.error

class InstallFailedVerificationTimeoutException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}