package com.rosan.installer.data.res.model.impl.res.arsc.config

enum class WideColorGamut(val value: UInt) {
    Any(0x00u),
    No(0x01u),
    Yes(0x02u);

    companion object {
        fun build(colorMode: UByte): WideColorGamut {
            val bits = colorMode.toUInt()
            return values().find { it.value and bits != 0x0u } ?: Any
        }
    }
}