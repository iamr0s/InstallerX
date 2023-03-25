package com.rosan.installer.data.res.util

import android.content.res.XmlResourceParser
import java.io.InputStream


class XmlResourceParserUtil {
    companion object {
        private val clazz = Class.forName("android.content.res.XmlBlock")

        private val constructor =
            clazz.getDeclaredConstructor(ByteArray::class.java)
                .also { it.isAccessible = true }

        private val method = clazz.getDeclaredMethod("newParser")
            .also { it.isAccessible = true }

        fun InputStream.parseAsXmlResourceParser(): XmlResourceParser? {
            return method.invoke(constructor.newInstance(this.readBytes())) as XmlResourceParser?
        }
    }
}

