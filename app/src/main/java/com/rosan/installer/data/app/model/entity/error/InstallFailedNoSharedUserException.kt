package com.rosan.installer.data.app.model.entity.error

class InstallFailedNoSharedUserException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}