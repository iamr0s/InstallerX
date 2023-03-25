package com.rosan.installer.data.res.model.impl.res.arsc.config

import com.rosan.installer.data.io.repo.ReaderRepo

enum class Navigation(val value: UByte) {
    Any(0x00u),
    Nonav(0x01u),
    Dpad(0x02u),
    Trackball(0x03u),
    Wheel(0x04u);

    companion object {
        fun build(repo: ReaderRepo): Navigation {
            return build(repo.uByte)
        }

        fun build(value: UByte): Navigation {
            return values().find { it.value == value }
                ?: Any
        }
    }
}