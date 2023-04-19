package com.rosan.installer.data.app.model.entity.error

class InstallFailedVerificationFailureException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}