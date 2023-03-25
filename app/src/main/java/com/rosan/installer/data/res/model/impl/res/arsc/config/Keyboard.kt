package com.rosan.installer.data.res.model.impl.res.arsc.config

import com.rosan.installer.data.io.repo.ReaderRepo

enum class Keyboard(val value: UByte) {
    Any(0x00u),
    Nokeys(0x01u),
    Qwerty(0x02u),
    _12Keys(0x03u);

    companion object {
        fun build(repo: ReaderRepo): Keyboard {
            return build(repo.uByte)
        }

        fun build(value: UByte): Keyboard {
            return values().find { it.value == value }
                ?: Any
        }
    }
}