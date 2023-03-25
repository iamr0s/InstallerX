package com.rosan.installer.data.res.model.impl.res.arsc.config

enum class LayoutDir(val value: UInt) {
    Any(0x00u),
    Ltr(0x01u),
    Rtl(0x02u);

    companion object {
        fun build(screenLayout: UByte): LayoutDir {
            val bits = screenLayout.toUInt() shr 6
            return values().find { it.value and bits != 0x0u } ?: Any
        }
    }
}