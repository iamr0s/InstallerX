package com.rosan.installer.data.app.model.entity.error

class InstallFailedMissingFeatureException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}