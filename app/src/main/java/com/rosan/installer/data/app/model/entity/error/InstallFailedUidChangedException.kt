package com.rosan.installer.data.app.model.entity.error

class InstallFailedUidChangedException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}