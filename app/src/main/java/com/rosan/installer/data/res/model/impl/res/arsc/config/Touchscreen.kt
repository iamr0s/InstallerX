package com.rosan.installer.data.res.model.impl.res.arsc.config

import com.rosan.installer.data.io.repo.ReaderRepo

enum class Touchscreen(val value: UByte) {
    Any(0x00u),
    Notouch(0x01u),
    Stylus(0x02u),
    Finger(0x03u);

    companion object {
        fun build(repo: ReaderRepo): Touchscreen {
            return build(repo.uByte)
        }

        fun build(value: UByte): Touchscreen {
            return values().find { it.value == value }
                ?: Any
        }
    }
}