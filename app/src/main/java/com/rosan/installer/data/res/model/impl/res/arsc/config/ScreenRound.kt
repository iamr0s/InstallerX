package com.rosan.installer.data.res.model.impl.res.arsc.config

enum class ScreenRound(val value: UInt) {
    Any(0x00u),
    No(0x01u),
    Yes(0x02u);

    companion object {
        fun build(screenLayout2: UByte): ScreenRound {
            val bits = screenLayout2.toUInt()
            return values().find { it.value and bits != 0x0u } ?: Any
        }
    }
}