package com.rosan.installer.data.res.model.impl

import android.util.TypedValue
import com.rosan.installer.data.common.model.entity.ErrorEntity
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.ValueEntity
import com.rosan.installer.data.res.model.entity.error.NotImplementError
import com.rosan.installer.data.res.model.impl.res.axml.XMLAttribute
import com.rosan.installer.data.res.model.impl.res.axml.XMLCData
import com.rosan.installer.data.res.model.impl.res.axml.XMLChunk
import com.rosan.installer.data.res.model.impl.res.axml.XMLEndElement
import com.rosan.installer.data.res.model.impl.res.axml.XMLEndNamespace
import com.rosan.installer.data.res.model.impl.res.axml.XMLPull
import com.rosan.installer.data.res.model.impl.res.axml.XMLStartElement
import com.rosan.installer.data.res.model.impl.res.axml.XMLStartNamespace
import com.rosan.installer.data.res.repo.AxmlPullRepo
import org.xmlpull.v1.XmlPullParser.END_DOCUMENT
import org.xmlpull.v1.XmlPullParser.END_TAG
import org.xmlpull.v1.XmlPullParser.FEATURE_PROCESS_NAMESPACES
import org.xmlpull.v1.XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES
import org.xmlpull.v1.XmlPullParser.START_DOCUMENT
import org.xmlpull.v1.XmlPullParser.START_TAG
import org.xmlpull.v1.XmlPullParser.TEXT
import java.io.InputStream
import java.io.Reader

class AxmlPullRepoImpl(reader: ReaderRepo) : AxmlPullRepo {
    private val xmlPull = XMLPull(reader)

    private var _depth = 0

    private var currentXmlChunk: XMLChunk? = null

    private var currentEventType: Int = START_DOCUMENT

    override fun setFeature(featureName: String?, state: Boolean) {
        if (FEATURE_PROCESS_NAMESPACES == featureName && state) return
        if (FEATURE_REPORT_NAMESPACE_ATTRIBUTES == featureName && state) return
        throw ErrorEntity("Unsupported feature: $featureName")
    }

    override fun getFeature(featureName: String?): Boolean {
        if (FEATURE_PROCESS_NAMESPACES == featureName) return true
        if (FEATURE_REPORT_NAMESPACE_ATTRIBUTES == featureName) return true
        return false
    }

    override fun setProperty(propertyName: String?, value: Any?) {
        throw ErrorEntity("setProperty() not supported")
    }

    override fun getProperty(propertyName: String?): Any? {
        return null
    }

    override fun setInput(reader: Reader?) {
        throw ErrorEntity("setInput() not supported")
    }

    override fun setInput(inputStream: InputStream?, inputEncoding: String?) {
        throw ErrorEntity("setInput() not supported")
    }

    override fun defineEntityReplacementText(entityName: String?, replacementText: String?) {
        throw ErrorEntity("defineEntityReplacementText() not supported")
    }

    override fun getNamespacePrefix(pos: Int): String {
        throw ErrorEntity("getNamespacePrefix() not supported")
    }

    override fun getInputEncoding(): String? {
        return null
    }

    override fun getNamespaceCount(p0: Int): Int {
        throw ErrorEntity("getNamespaceCount() not supported")
    }

    override fun getPositionDescription(): String {
        return "Binary XML file line #$lineNumber"
    }

    override fun getNamespaceUri(p0: Int): String {
        throw NotImplementError("getNamespaceUri() not supported")
    }

    override fun getNamespace(prefix: String?): String {
        throw NotImplementError("getNamespace() not supported")
    }

    override fun getColumnNumber(): Int {
        return -1
    }

    override fun getDepth(): Int {
        return _depth
    }

    override fun getText(): String? {
        val xmlChunk = currentXmlChunk
        if (xmlChunk !is XMLCData) return null
        return xmlPull.stringPool.getRawString(xmlChunk.data)
    }

    override fun getLineNumber(): Int {
        return currentXmlChunk?.lineNumber ?: -1
    }

    override fun isWhitespace(): Boolean {
        return false
    }

    override fun getPrefix(): String {
        throw ErrorEntity("getPrefix not supported")
    }

    override fun getTextCharacters(holderForStartAndLength: IntArray?): CharArray? {
        val txt = text
        if (txt != null) {
            holderForStartAndLength?.set(0, 0)
            holderForStartAndLength?.set(0, txt.length)
            return txt.toCharArray()
        }
        return null
    }

    private fun getElementNamespaceID(): Int {
        currentXmlChunk?.let {
            when (it) {
                is XMLStartElement -> {
                    return it.uri
                }
                is XMLEndElement -> {
                    return it.uri
                }
                else -> {}
            }
        }
        return -1
    }

    override fun getNamespace(): String {
        return xmlPull.stringPool.getRawString(getElementNamespaceID()) ?: ""
    }

    private fun getElementNameID(): Int {
        currentXmlChunk?.let {
            when (it) {
                is XMLStartElement -> {
                    return it.name
                }
                is XMLEndElement -> {
                    return it.name
                }
                else -> {}
            }
        }
        return -1
    }

    override fun getName(): String? {
        return xmlPull.stringPool.getRawString(getElementNameID())
    }

    override fun isEmptyElementTag(): Boolean {
        return false
    }

    override fun getAttributeCount(): Int {
        val xmlChunk = currentXmlChunk
        return if (xmlChunk is XMLStartElement) xmlChunk.attributes.size else 0
    }

    override fun getAttributeNamespace(index: Int): String? {
        val defValue = null
        val xmlChunk = currentXmlChunk
        if (xmlChunk is XMLStartElement) {
            val idx = xmlChunk.attributes.getOrNull(index)?.namespace ?: return defValue
            return xmlPull.stringPool.getRawString(idx) ?: return defValue
        }
        return defValue
    }

    override fun getAttributeName(index: Int): String? {
        val xmlChunk = currentXmlChunk
        if (xmlChunk is XMLStartElement) {
            val name = xmlChunk.attributes.getOrNull(index)?.name ?: return null
            return xmlPull.stringPool.getRawString(name)
        }
        return null
    }

    override fun getAttributePrefix(p0: Int): String {
        throw NotImplementError()
    }

    override fun getAttributeType(index: Int): String {
        return "CDATA"
    }

    override fun isAttributeDefault(index: Int): Boolean {
        return false
    }

    override fun getAttributeValue(index: Int): String? {
        getAttribute(index)?.let {
            val id = it.rawValue
            if (id > 0) return xmlPull.stringPool.getRawString(id)
            if (it.typedValue.type == ValueEntity.Type.NULL) throw ErrorEntity("Index out of bounds: $index")
            return TypedValue.coerceToString(it.typedValue.type.value.toInt(), it.typedValue.data)
        }
        return null
    }

    override fun getAttributeValue(ns: String?, nm: String?): String? {
        val idx = getAttributeIndex(ns, nm)
        if (idx >= 0) {
            return getAttributeValue(idx)
        }
        return null
    }

    override fun getEventType(): Int {
        return currentEventType
    }

    override fun nextToken(): Int {
        return next()
    }

    override fun next(): Int {
        val xmlChunk = xmlPull.next()
        currentXmlChunk = xmlChunk
        val oldEventType = currentEventType
        currentEventType = when (xmlChunk) {
            is XMLStartNamespace -> START_DOCUMENT
            is XMLEndNamespace -> END_DOCUMENT
            is XMLStartElement -> START_TAG
            is XMLEndElement -> END_TAG
            is XMLCData -> TEXT
            else -> END_DOCUMENT
        }
        if (
            (oldEventType == START_DOCUMENT || oldEventType == START_TAG)
            && (currentEventType == START_TAG || currentEventType == TEXT)
        ) {
            _depth += 1
        } else if (
            (oldEventType == END_TAG || oldEventType == TEXT)
            && (currentEventType == END_TAG || currentEventType == END_DOCUMENT)
        ) {
            _depth -= 1
        }
        return currentEventType
    }

    override fun require(type: Int, ns: String?, nm: String?) {
        if (type != eventType
            || (ns != null && ns != namespace)
            || (nm != null && nm != name)
        ) throw ErrorEntity("expected type: $type")
    }

    override fun nextText(): String? {
        if (eventType == START_TAG) throw ErrorEntity("$positionDescription: parser must be on START_TAG to read next text")
        var newEventType = next()
        return when (newEventType) {
            TEXT -> {
                val result = text
                newEventType = next()
                if (newEventType != END_TAG) throw ErrorEntity("$positionDescription: event TEXT it must be immediately followed by END_TAG")
                result
            }
            END_TAG -> {
                ""
            }
            else -> {
                throw ErrorEntity("$positionDescription: parser must be on START_TAG or TEXT to read text")
            }
        }
    }

    override fun nextTag(): Int {
        var eventType = next()
        if (eventType == TEXT && isWhitespace) eventType = next()
        if (eventType != START_TAG && eventType != END_TAG) {
            throw ErrorEntity("$positionDescription: expected start or end tag")
        }
        return eventType
    }

    override fun getAttributeTypedValue(index: Int): TypedValue {
        val value = getAttribute(index) ?: throw ErrorEntity("index out of the bouds")
        return TypedValue().also {
            it.type = value.typedValue.type.value.toInt()
            it.data = value.typedValue.data
            it.resourceId = value.rawValue
            if (it.resourceId == -1) it.resourceId = it.data
            if (value.typedValue.type == ValueEntity.Type.STRING) {
                it.string = xmlPull.stringPool.getRawString(it.data)
            }
        }
    }

    private fun getAttributeNameID(index: Int): Int {
        return getAttribute(index)?.name ?: -1
    }

    override fun getAttributeNameResource(index: Int): Int {
        val id = getAttributeNameID(index)
        if (id < 0) return -1
        return xmlPull.resourceMap.getOrElse(id) { -1 }
    }

    override fun getAttributeListValue(
        ns: String?,
        nm: String?,
        options: Array<out String>,
        defaultValue: Int
    ): Int {
        val idx = getAttributeIndex(ns, nm)
        if (idx >= 0) return getAttributeListValue(idx, options, defaultValue)
        return defaultValue
    }

    override fun getAttributeListValue(
        idx: Int,
        options: Array<out String>,
        defaultValue: Int
    ): Int {
        getAttribute(idx)?.let {
            if (it.typedValue.type == ValueEntity.Type.STRING) {
                val value = xmlPull.stringPool.getRawString(it.typedValue.data)
                if (value?.isNotEmpty() == true) {
                    for (i in options.indices) {
                        val c = options[i]
                        if (value == c) {
                            return i
                        }
                    }
                }
            }
        }
        return defaultValue
    }

    override fun getAttributeBooleanValue(
        ns: String?,
        nm: String?,
        defaultValue: Boolean
    ): Boolean {
        val idx = getAttributeIndex(ns, nm)
        if (idx >= 0) return getAttributeBooleanValue(idx, defaultValue)
        return defaultValue
    }

    override fun getAttributeBooleanValue(idx: Int, defaultValue: Boolean): Boolean {
        getAttribute(idx)?.let {
            if (it.typedValue.type.value >= ValueEntity.Type.FIRST_INT
                && it.typedValue.type.value <= ValueEntity.Type.LAST_INT
            ) {
                return it.typedValue.data != 0
            }
        }
        return defaultValue
    }

    override fun getAttributeResourceValue(
        ns: String?,
        nm: String?,
        defaultValue: Int
    ): Int {
        val idx = getAttributeIndex(ns, nm)
        if (idx >= 0) return getAttributeResourceValue(idx, defaultValue)
        return defaultValue
    }

    override fun getAttributeResourceValue(idx: Int, defaultValue: Int): Int {
        getAttribute(idx)?.let {
            if (it.typedValue.type == ValueEntity.Type.REFERENCE) {
                return it.typedValue.data
            }
        }
        return defaultValue
    }

    override fun getAttributeIntValue(ns: String?, nm: String?, defaultValue: Int): Int {
        val idx = getAttributeIndex(ns, nm)
        if (idx >= 0) return getAttributeIntValue(idx, defaultValue)
        return defaultValue
    }

    override fun getAttributeIntValue(idx: Int, defaultValue: Int): Int {
        getAttribute(idx)?.let {
            if (it.typedValue.type.value >= ValueEntity.Type.FIRST_INT
                && it.typedValue.type.value <= ValueEntity.Type.LAST_INT
            ) {
                return it.typedValue.data
            }
        }
        return defaultValue
    }

    override fun getAttributeUnsignedIntValue(
        ns: String?,
        nm: String?,
        defaultValue: Int
    ): Int {
        val idx = getAttributeIndex(ns, nm)
        if (idx >= 0) return getAttributeUnsignedIntValue(idx, defaultValue)
        return defaultValue
    }

    override fun getAttributeUnsignedIntValue(idx: Int, defaultValue: Int): Int {
        getAttribute(idx)?.let {
            if (it.typedValue.type.value >= ValueEntity.Type.FIRST_INT
                && it.typedValue.type.value <= ValueEntity.Type.LAST_INT
            ) {
                return it.typedValue.data
            }
        }
        return defaultValue
    }

    override fun getAttributeFloatValue(
        ns: String?,
        nm: String?,
        defaultValue: Float
    ): Float {
        val idx = getAttributeIndex(ns, nm)
        if (idx >= 0) return getAttributeFloatValue(idx, defaultValue)
        return defaultValue
    }

    override fun getAttributeFloatValue(idx: Int, defaultValue: Float): Float {
        getAttribute(idx)?.let {
            if (it.typedValue.type == ValueEntity.Type.FLOAT) {
                return java.lang.Float.intBitsToFloat(it.typedValue.data)
            }
        }
        return defaultValue
    }

    private fun indexOfID(): Int {
        val xmlChunk = currentXmlChunk
        if (xmlChunk is XMLStartElement) {
            return xmlChunk.idIndex - 1
        }
        return -1
    }

    private fun indexOfClass(): Int {
        val xmlChunk = currentXmlChunk
        if (xmlChunk is XMLStartElement) {
            return xmlChunk.classIndex - 1
        }
        return -1
    }

    private fun indexOfStyle(): Int {
        val xmlChunk = currentXmlChunk
        if (xmlChunk is XMLStartElement) {
            return xmlChunk.styleIndex - 1
        }
        return -1
    }

    private fun getAttributeIndex(ns: String?, nm: String?): Int {
        for (i in 0 until attributeCount) {
            if (ns == getAttributeNamespace(i)
                && nm == getAttributeName(i)
            ) {
                return i
            }
        }
        return -1
    }

    // see http://www.aospxref.com/android-13.0.0_r3/xref/frameworks/base/libs/androidfw/ResourceTypes.cpp?fi=getAttributeValueStringID#1423
    private fun getAttribute(index: Int): XMLAttribute? {
        val xmlChunk = currentXmlChunk
        if (xmlChunk is XMLStartElement) {
            if (index >= 0 && index < xmlChunk.attributes.size) {
                return xmlChunk.attributes[index]
            }
        }
        return null
    }

    override fun getIdAttribute(): String? {
        getAttribute(indexOfID())?.let {
            xmlPull.stringPool.getRawString(it.rawValue)
        }
        return null
    }

    override fun getClassAttribute(): String? {
        getAttribute(indexOfClass())?.let {
            xmlPull.stringPool.getRawString(it.rawValue)
        }
        return null
    }

    override fun getIdAttributeResourceValue(defaultValue: Int): Int {
        return getAttributeResourceValue(null, "id", defaultValue)
    }

    override fun getStyleAttribute(): Int {
        getAttribute(indexOfStyle())?.let {
            val value = it.typedValue
            return if (value.type == ValueEntity.Type.REFERENCE || value.type == ValueEntity.Type.ATTRIBUTE)
                value.data
            else 0
        }
        return 0
    }

    override fun close() {
    }
}