package com.rosan.installer.data.res.model.impl.res.arsc.config

enum class UIModeType(val value: UInt) {
    Any(0x00u),
    Normal(0x01u),
    Desk(0x02u),
    Car(0x03u),
    Television(0x04u),
    Appliance(0x05u),
    Watch(0x06u),
    VRHeadset(0x07u);

    companion object {
        fun build(uiMode: UByte): UIModeType {
            val bits = uiMode.toUInt()
            return values().find { it.value and bits != 0x0u } ?: Normal
        }
    }
}