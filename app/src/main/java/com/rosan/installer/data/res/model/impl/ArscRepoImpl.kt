package com.rosan.installer.data.res.model.impl

import android.content.res.Configuration
import android.os.Build
import androidx.core.text.parseAsHtml
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.Entry
import com.rosan.installer.data.res.model.entity.MapEntryEntity
import com.rosan.installer.data.res.model.entity.ValueEntity
import com.rosan.installer.data.res.model.impl.res.ResEntry
import com.rosan.installer.data.res.model.impl.res.arsc.Table
import com.rosan.installer.data.res.model.impl.res.arsc.TablePackage
import com.rosan.installer.data.res.model.impl.res.arsc.TableType
import com.rosan.installer.data.res.model.impl.res.arsc.config.Density
import com.rosan.installer.data.res.model.impl.res.arsc.config.Orientation
import com.rosan.installer.data.res.model.impl.res.arsc.config.UIModeNight
import com.rosan.installer.data.res.model.impl.res.arsc.config.UIModeType
import com.rosan.installer.data.res.model.impl.res.arsc.entry.ResMapEntry
import com.rosan.installer.data.res.model.impl.res.entry.ResValueEntry
import com.rosan.installer.data.res.repo.ArscRepo
import com.rosan.installer.data.res.util.config.sortedByDensity
import com.rosan.installer.data.res.util.config.sortedByLocales
import com.rosan.installer.data.res.util.config.sortedByOrientation
import com.rosan.installer.data.res.util.config.sortedBySdkVersion
import com.rosan.installer.data.res.util.config.sortedByUIModeNight
import com.rosan.installer.data.res.util.config.sortedByUIModeType
import java.util.*


class ArscRepoImpl(reader: ReaderRepo) : ArscRepo {
    private val table = Table.build(reader)

    private fun findPackage(packageId: Int): TablePackage? {
        return table.packages.find { it.id == packageId }
    }

    fun findType(
        packageId: Int,
        typeId: Int,
        valueId: Int,
        configuration: Configuration,
        densityDpi: Int? = null
    ): TableType? {
        val default = null
        val `package` = findPackage(packageId) ?: return default
        val spec = `package`.typeSpecMap[typeId]?.entries?.getOrNull(valueId)
        var types = `package`.typeMap[typeId]?.filter {
            it.entryMap.containsKey(valueId)
        } ?: return default
        if (spec == null || spec.hasLocale) {
            val supportLocales = mutableListOf<Locale>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val locales = configuration.locales
                for (i in 0 until locales.size()) {
                    supportLocales.add(locales.get(i))
                }
            } else {
                supportLocales.add(configuration.locale)
            }
            types = types.sortedByLocales(supportLocales)
        }
        if (spec == null || spec.hasDensity) {
            types = types.sortedByDensity(
                when (densityDpi) {
                    null -> configuration.densityDpi
                    Density.Default.value, Density.None.value, Density.Any.value -> configuration.densityDpi
                    else -> densityDpi
                }
            )
        }
        if (spec == null || spec.hasOrientation) {
            types =
                types.sortedByOrientation(Orientation.build(configuration.orientation.toUByte()))
        }
        if (spec == null || spec.hasUIMode) {
            val setUIModeType = UIModeType.build(configuration.uiMode.toUByte())
            val setUIModeNight = UIModeNight.build(configuration.uiMode.toUByte())
            types = types.sortedByUIModeType(setUIModeType).sortedByUIModeNight(setUIModeNight)
        }
        if (spec == null || spec.hasVersion) {
            types = types.sortedBySdkVersion(Build.VERSION.SDK_INT)
        }
        return types.firstOrNull()
    }

    private fun findValue(
        packageId: Int,
        typeId: Int,
        valueId: Int,
        configuration: Configuration,
        densityDpi: Int?
    ): ResEntry? {
        return findType(packageId, typeId, valueId, configuration, densityDpi)?.entryMap?.get(
            valueId
        )
    }

    override fun getValue(
        packageId: Int,
        typeId: Int,
        valueId: Int,
        configuration: Configuration,
        densityDpi: Int?
    ): Entry? {
        val resEntry =
            findValue(packageId, typeId, valueId, configuration, densityDpi) ?: return null
        return when (resEntry) {
            is ResValueEntry -> {
                ValueEntity(ValueEntity.Type.build(resEntry.value.type.value), resEntry.value.data)
            }
            is ResMapEntry -> {
                val values = mutableMapOf<Int, ValueEntity>()
                resEntry.valueMap.forEach {
                    values[it.key] =
                        ValueEntity(ValueEntity.Type.build(it.value.type.value), it.value.data)
                }
                MapEntryEntity(
                    resEntry.parent,
                    values
                )
            }
            else -> {
                null
            }
        }
    }

    override fun getValueName(
        packageId: Int,
        typeId: Int,
        valueId: Int,
        configuration: Configuration,
        densityDpi: Int?
    ): CharSequence? {
        val key =
            findValue(packageId, typeId, valueId, configuration, densityDpi)?.key ?: return null
        return findPackage(packageId)?.keyStringPool?.getHtmlString(key)?.parseAsHtml()
    }

    override fun getTypeName(packageId: Int, typeId: Int): CharSequence? {
        return findPackage(packageId)?.typeStringPool?.getHtmlString(typeId)?.parseAsHtml()
    }

    override fun getPackageName(packageId: Int): CharSequence? {
        return findPackage(packageId)?.name
    }

    override fun getString(index: Int): CharSequence? {
        return table.stringPool.getHtmlString(index)?.parseAsHtml()
    }
}