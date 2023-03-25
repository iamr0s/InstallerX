package com.rosan.installer.data.res.util

import android.content.res.ColorStateList
import android.content.res.ComplexColor
import android.content.res.GradientColor
import android.content.res.Resources
import android.content.res.TypedArray
import android.content.res.TypedArrayProxy2
import android.content.res.XmlResourceParser
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.RequiresApi
import com.rosan.installer.data.common.model.entity.ErrorEntity
import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.ValueEntity
import com.rosan.installer.data.res.repo.ArscRepo
import com.rosan.installer.data.res.repo.AxmlPullRepo
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

abstract class ResourcesProxy(
    private val repo: ArscRepo,
    private val parent: Resources = getSystem()
) :
    Resources(parent.assets, parent.displayMetrics, parent.configuration), KoinComponent {

    abstract fun loadBytes(path: String): ByteArray?

    abstract fun loadInputStream(path: String): InputStream?

    override fun getText(id: Int): CharSequence {
        val ret = getText(id, null)
        if (ret != null) return ret
        return parent.getText(id)
    }

    override fun getText(id: Int, def: CharSequence?): CharSequence? {
        val value = TypedValue()
        getValue(id, value, true)
        if (value.type == TypedValue.TYPE_STRING) return value.string
        return parent.getText(id, def)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getColor(id: Int, theme: Theme?): Int {
        val value = TypedValue()
        getValue(id, value, true)
        when (value.type) {
            in TypedValue.TYPE_FIRST_INT..TypedValue.TYPE_LAST_INT -> return value.data
            TypedValue.TYPE_STRING -> return getColorStateList(id, theme).defaultColor
        }
        return parent.getColor(id, theme)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getColorStateList(id: Int, theme: Theme?): ColorStateList {
        return loadComplexColor(TypedValue(), id, theme) as ColorStateList
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Override
    fun loadComplexColor(value: TypedValue, id: Int, theme: Theme?): ComplexColor? {
        getValue(id, value, true)
        when (value.type) {
            in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT -> {
                return ColorStateList.valueOf(value.data) as ComplexColor?
            }
            TypedValue.TYPE_STRING -> {
                val path = value.string.toString()
                val xml = getXml(path)
                var type: Int = -1
                while (xml.next().also { type = it } != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) {
                    // seek xml to start tag
                }
                when (xml.name) {
                    "gradient" -> return GradientColor.createFromXml(this, getXml(path), theme)
                    "selector" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        getXml(path).let {
                            ColorStateList.createFromXml(
                                this,
                                it,
                                theme
                            ) as ComplexColor?
                        }
                    }
                }
            }
        }
        return parent.getColorStateList(id, theme) as ComplexColor?
    }

    override fun getBoolean(id: Int): Boolean {
        val value = TypedValue()
        getValue(id, value, true)
        when (value.type) {
            in TypedValue.TYPE_FIRST_INT..TypedValue.TYPE_LAST_INT -> return value.data != 0
        }
        return parent.getBoolean(id)
    }

    override fun getInteger(id: Int): Int {
        val value = TypedValue()
        getValue(id, value, true)
        when (value.type) {
            in TypedValue.TYPE_FIRST_INT..TypedValue.TYPE_LAST_INT -> return value.data
        }
        return parent.getInteger(id)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun getFloat(id: Int): Float {
        val value = TypedValue()
        getValue(id, value, true)
        when (value.type) {
            in TypedValue.TYPE_FIRST_INT..TypedValue.TYPE_LAST_INT -> return value.float
        }
        return parent.getFloat(id)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("getDrawable(id, null)"))
    override fun getDrawable(id: Int): Drawable? {
        return getDrawable(id, null)
    }

    override fun getDrawable(id: Int, theme: Theme?): Drawable? {
        return getDrawableForDensity(id, displayMetrics.densityDpi, theme)
    }

    @Deprecated("Deprecated in Java", ReplaceWith("getDrawableForDensity(id, density, null)"))
    override fun getDrawableForDensity(id: Int, density: Int): Drawable? {
        return getDrawableForDensity(id, density, null)
    }

    override fun getDrawableForDensity(id: Int, density: Int, theme: Theme?): Drawable? {
        val drawable = loadDrawable(TypedValue(), id, density, theme)
        if (drawable != null) return drawable
        return super.getDrawableForDensity(id, density, theme)
    }

    @Override
    fun loadDrawable(value: TypedValue, id: Int, density: Int, theme: Theme?): Drawable? {
        getValueForDensity(id, density, value, true)
        when (value.type) {
            TypedValue.TYPE_STRING -> {
                val path = value.string.toString()
                return if (path.endsWith(".xml")) Drawable.createFromXml(this, getXml(path), theme)
                else Drawable.createFromResourceStream(this, null, loadInputStream(path), path)
            }
            in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT -> return ColorDrawable(
                value.data
            )
        }
        return null
    }

    override fun obtainAttributes(set: AttributeSet, attrs: IntArray): TypedArray {
        return TypedArrayProxy2(this, set as AxmlPullRepo, attrs)
    }

    override fun getValue(id: Int, outValue: TypedValue?, resolveRefs: Boolean) {
        if (outValue != null) {
            val value = repo.getValue(id, configuration)
            if (value != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    outValue.sourceResourceId = id
                }
                if (value is ValueEntity) {
                    outValue.type = value.type.value.toInt()
                    outValue.data = value.data
                    when (value.type) {
                        ValueEntity.Type.STRING -> {
                            outValue.string = repo.getString(value.data)
                        }
                        ValueEntity.Type.REFERENCE -> {
                            if (resolveRefs) {
                                getValue(value.data, outValue, resolveRefs)
                            }
                        }
                        else -> {}
                    }
                }
                return
            }
        }
        kotlin.runCatching {
            parent.getValue(id, outValue, resolveRefs)
        }
    }

    override fun getValueForDensity(
        id: Int,
        density: Int,
        outValue: TypedValue?,
        resolveRefs: Boolean
    ) {
        if (outValue != null) {
            val value = repo.getValue(id, configuration, density)
            if (value != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    outValue.sourceResourceId = id
                }
                if (value is ValueEntity) {
                    outValue.type = value.type.value.toInt()
                    outValue.data = value.data
                    outValue.density = density
                    when (value.type) {
                        ValueEntity.Type.STRING -> {
                            outValue.string = repo.getString(value.data)
                        }
                        ValueEntity.Type.REFERENCE -> {
                            if (resolveRefs) {
                                getValue(value.data, outValue, resolveRefs)
                            }
                        }
                        else -> {}
                    }
                }
                return
            }
        }
        parent.getValueForDensity(id, density, outValue, resolveRefs)
    }

    override fun getXml(id: Int): XmlResourceParser {
        loadXml(id)?.let {
            return it
        }
        return parent.getXml(id)
    }

    private fun loadXml(id: Int): XmlResourceParser? {
        val value = TypedValue()
        getValue(id, value, true)
        if (value.type == TypedValue.TYPE_STRING) {
            val path = value.string.toString()
            return getXml(path)
        }
        return parent.getXml(id)
    }

    private fun getXml(path: String): XmlResourceParser {
        return loadXml(path) ?: throw ErrorEntity("'$path' not found")
    }

    private fun loadXml(path: String): XmlResourceParser? {
        return loadInputStream(path)?.readerRepo()?.let {
            it.endian = ReaderRepo.Endian.Little
            return@let get<AxmlPullRepo> {
                parametersOf(it)
            }
        }
//        return loadInputStream(path)?.parseAsXmlResourceParser()
    }
}