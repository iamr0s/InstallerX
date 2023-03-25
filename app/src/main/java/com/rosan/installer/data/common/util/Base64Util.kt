package com.rosan.installer.data.common.util

import android.util.Base64

fun ByteArray.base64(flags: Int): ByteArray {
    return Base64.encode(this, flags)
}

fun ByteArray.base64String(flags: Int): String {
    return Base64.encodeToString(this, flags)
}

fun String.unbase64(flags: Int): ByteArray {
    return Base64.decode(this, flags)
}

fun ByteArray.unbase64(flags: Int): ByteArray {
    return Base64.decode(this, flags)
}
