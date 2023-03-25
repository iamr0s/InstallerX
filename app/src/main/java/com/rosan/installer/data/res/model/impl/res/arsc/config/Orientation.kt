package com.rosan.installer.data.res.model.impl.res.arsc.config

import com.rosan.installer.data.io.repo.ReaderRepo

enum class Orientation(val value: UByte) {
    Any(0x00u),
    Port(0x01u),
    Land(0x02u),
    Square(0x03u);

    companion object {
        fun build(repo: ReaderRepo): Orientation {
            return build(repo.uByte)
        }

        fun build(value: UByte): Orientation {
            return values().find { it.value == value }
                ?: Any
        }
    }
}