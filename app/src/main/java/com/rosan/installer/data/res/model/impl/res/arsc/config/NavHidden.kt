package com.rosan.installer.data.res.model.impl.res.arsc.config

import com.rosan.installer.data.io.repo.ReaderRepo

enum class NavHidden(val value: UByte) {
    Any((0x00u shl 2).toUByte()),
    No((0x01u shl 2).toUByte()),
    Yes((0x02u shl 2).toUByte());

    companion object {
        fun build(repo: ReaderRepo): NavHidden {
            return build(repo.uByte)
        }

        fun build(value: UByte): NavHidden {
            return values().find { it.value == value }
                ?: Any
        }
    }
}