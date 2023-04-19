package com.rosan.installer.data.app.model.entity.error

class InstallFailedNewerSDKException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}