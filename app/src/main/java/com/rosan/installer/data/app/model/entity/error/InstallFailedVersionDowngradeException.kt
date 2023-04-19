package com.rosan.installer.data.app.model.entity.error

class InstallFailedVersionDowngradeException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}