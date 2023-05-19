package com.rosan.installer.data.app.model.exception

class RootNotWorkException : RuntimeException {
    constructor() : super()

    constructor(message: String?) : super(message)

    constructor(cause: Throwable?) : super(cause)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
