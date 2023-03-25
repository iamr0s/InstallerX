package com.rosan.installer.data.res.model.impl.res.arsc.config

import com.rosan.installer.data.io.repo.ReaderRepo

enum class KeysHidden(val value: UByte) {
    Any(0x00u),
    No(0x01u),
    Yes(0x02u),
    Soft(0x03u);

    companion object {
        fun build(repo: ReaderRepo): KeysHidden {
            return build(repo.uByte)
        }

        fun build(value: UByte): KeysHidden {
            return values().find { it.value == value }
                ?: Any
        }
    }
}