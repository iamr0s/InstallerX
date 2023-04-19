package com.rosan.installer.data.app.model.entity.error

class InstallFailedInvalidAPKException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}