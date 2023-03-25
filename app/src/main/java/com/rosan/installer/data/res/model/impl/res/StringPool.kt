package com.rosan.installer.data.res.model.impl.res

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.io.util.readString
import com.rosan.installer.data.res.model.impl.res.header.ChunkHeader
import com.rosan.installer.data.res.model.impl.res.header.StringPoolHeader

class StringPool(
    private val flags: StringPoolHeader.Flags,
    private val stringsBytes: ByteArray,
    private val stringOffsets: List<Int>,
    private val stylesBytes: ByteArray,
    private val stylesOffsets: List<Int>,
) {
    /**
     * @url http://www.aospxref.com/android-13.0.0_r3/xref/frameworks/base/libs/androidfw/include/androidfw/ResourceTypes.h#490
     * a span of style information with a string in the StringPool
     * */
    class Span(
        val name: Int,
        val firstChar: Int,
        val lastChar: Int,
    ) : Comparable<Span> {
        override fun compareTo(other: Span): Int {
            var ret = firstChar - other.firstChar
            if (ret != 0) return ret
            ret = lastChar - other.lastChar
            if (ret != 0) return ret
            return 0
        }
    }

    class Style(
        val spans: List<Span>
    ) {
        companion object {
            /**
             * @url http://www.aospxref.com/android-13.0.0_r3/xref/frameworks/base/libs/androidfw/include/androidfw/ResourceTypes.h#493
             * the special value END (unsigned int: 0xFFFFFFFF, int: -1) indicates the end of an array of spans.
             * */
            fun build(repo: ReaderRepo): Style {
                val spans = mutableListOf<Span>()
                while (true) {
                    val name = repo.int
                    if (name == -1) break
                    val firstChar = repo.int
                    val lastChar = repo.int
                    spans.add(Span(name, firstChar, lastChar))
                }
                return Style(spans)
            }
        }
    }

    private val _cachedStyleMap = mutableMapOf<Int, Style>()

    private val _cachedStringMap = mutableMapOf<Int, String>()

    fun getStyle(index: Int): Style? {
        _cachedStyleMap[index]?.also {
            return it
        }
        val pos = stylesOffsets.getOrNull(index) ?: return null
        if (pos > stylesBytes.size) return null
        return stylesBytes.inputStream(pos, stylesBytes.size - pos).readerRepo().use {
            it.endian = ReaderRepo.Endian.Little
            Style.build(it)
        }.also {
            _cachedStyleMap[index] = it
        }
    }

    fun getRawString(index: Int): String? {
        _cachedStringMap[index]?.also {
            return it
        }
        val pos = stringOffsets.getOrNull(index) ?: return null
        if (pos > stringsBytes.size) return null
        return stringsBytes.inputStream(pos, stringsBytes.size - pos).readerRepo().use {
            it.endian = ReaderRepo.Endian.Little
            if (flags.isUTF8) {
                val charsLength = lengthUTF8(it)
                val bytesLength = lengthUTF8(it)
                it.read(bytesLength).readString(charset = Charsets.UTF_8)
            } else {
                val charsLength = lengthUTF16(it)
                val bytesLength = charsLength * 2
                it.read(bytesLength).readString(charset = Charsets.UTF_16LE)
            }
        }.also {
            _cachedStringMap[index] = it
        }
    }

    fun getHtmlString(index: Int): String? {
        val raw = getRawString(index) ?: return null
        val style = getStyle(index) ?: return raw

        /**
         * @items struct
         * [
         *     [char index, name index, start tag or end tag (0 or other)]
         * ]
         */
        var ret = ""
        var previous = 0
        mutableListOf<List<Int>>().also { items ->
            style.spans.forEach {
                items.add(listOf(it.firstChar, it.name, 0))
                items.add(listOf(it.lastChar + 1, it.name, 1))
            }
        }.sortedBy {
            it[0]
        }.forEach {
            val charIndex = it[0]
            val nameAndAttrs = getRawString(it[1]) ?: ""
            var separatorIndex = nameAndAttrs.indexOf(';')
            if (separatorIndex == -1) separatorIndex = nameAndAttrs.length
            val name = nameAndAttrs.subSequence(0, separatorIndex)
            val start = it[2] == 0
            ret += raw.substring(previous, charIndex)
            if (start) {
                ret += "<$name"

                // add attrs when it have
                if (nameAndAttrs.length > separatorIndex) {
                    mutableMapOf<String, String>().also { attrs ->
                        nameAndAttrs.subSequence(separatorIndex + 1, nameAndAttrs.length).split(';')
                            .forEach {
                                val equalSepIndex = it.indexOf('=')
                                attrs[it.substring(0, equalSepIndex)] =
                                    it.substring(equalSepIndex + 1, it.length)
                            }
                    }.forEach {
                        ret += " ${it.key}=\"${it.value}\""
                    }
                }

                ret += ">"
            } else ret += "</$name>"
            previous = charIndex
        }

        return ret
    }

    companion object {
        val empty = StringPool(
            StringPoolHeader.Flags(0u),
            byteArrayOf(),
            listOf(),
            byteArrayOf(),
            listOf()
        )

        fun build(repo: ReaderRepo, headerOrNull: StringPoolHeader? = null): StringPool {
            val header = headerOrNull ?: StringPoolHeader.build(repo, ChunkHeader.build(repo))
            val reader = repo.readerRepo()
            val stringsOffset = mutableListOf<Int>()
            for (i in 0 until header.stringCount) {
                if (header.stringsStart > header.length + reader.used) stringsOffset.add(reader.int)
                else break
            }

            val stylesOffset = mutableListOf<Int>()
            for (i in 0 until header.styleCount) {
                if (header.stringsStart > header.length + reader.used) stylesOffset.add(reader.int)
                else break
            }

            // see http://www.aospxref.com/android-13.0.0_r3/xref/frameworks/base/libs/androidfw/ResourceTypes.cpp?fi=ResXMLParser#538
            if (header.stringsStart > header.length + reader.used) reader.skip(header.stringsStart - header.length - reader.used)
            val stringsBytes = if (header.stringCount > 0) reader.read(
                if (header.styleCount > 0)
                    header.stylesStart - header.stringsStart
                else
                    header.chunkSize - header.stringsStart
            ) else byteArrayOf()

            if (header.stylesStart > header.length + reader.used) reader.skip(header.stylesStart - header.length - reader.used)
            val stylesBytes =
                if (header.styleCount > 0) reader.read(header.chunkSize - header.stylesStart) else byteArrayOf()

            if (header.chunkSize > header.length + reader.used) reader.skip(header.chunkSize - header.length - reader.used)
            return StringPool(
                header.flags,
                stringsBytes,
                stringsOffset,
                stylesBytes,
                stylesOffset
            )
        }

        /**
         * @url http://www.aospxref.com/android-13.0.0_r3/xref/frameworks/base/libs/androidfw/ResourceTypes.cpp#738
         * */
        fun lengthUTF8(repo: ReaderRepo): Int {
            var length = repo.uByte.toInt()
            if (length and 0x80 != 0x0)
                length = ((length and 0x7F) shl 8) or repo.uByte.toInt()
            return length
        }

        /**
         * @url http://www.aospxref.com/android-13.0.0_r3/xref/frameworks/base/libs/androidfw/ResourceTypes.cpp#710
         * */
        private fun lengthUTF16(repo: ReaderRepo): Int {
            var length = repo.uShort.toInt()
            if (length and 0x8000 != 0x0)
                length = ((length and 0x7FFF) shl 16) or repo.uShort.toInt()
            return length
        }
    }
}