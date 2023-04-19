package com.rosan.installer.data.app.model.entity.error

class InstallFailedTestOnlyException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}