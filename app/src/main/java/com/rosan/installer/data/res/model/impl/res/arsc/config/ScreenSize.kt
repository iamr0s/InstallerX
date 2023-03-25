package com.rosan.installer.data.res.model.impl.res.arsc.config

enum class ScreenSize(val value: UInt) {
    Any(0x00u),
    Small(0x01u),
    Normal(0x02u),
    Large(0x03u),
    XLarge(0x04u);

    companion object {
        fun build(screenLayout: UByte): ScreenSize {
            val bits = screenLayout.toUInt()
            return values().find { it.value and bits != 0x0u } ?: Any
        }
    }
}