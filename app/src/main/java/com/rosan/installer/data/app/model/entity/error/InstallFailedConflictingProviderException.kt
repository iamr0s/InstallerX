package com.rosan.installer.data.app.model.entity.error

class InstallFailedConflictingProviderException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}