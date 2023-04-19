package com.rosan.installer.data.app.model.entity.error

class InstallFailedCpuAbiIncompatibleException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)
}