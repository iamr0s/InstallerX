package com.rosan.installer.data.res.model.impl.res.arsc

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.io.util.readByte
import com.rosan.installer.data.io.util.readString
import com.rosan.installer.data.res.model.impl.res.arsc.config.Density
import com.rosan.installer.data.res.model.impl.res.arsc.config.HDR
import com.rosan.installer.data.res.model.impl.res.arsc.config.Keyboard
import com.rosan.installer.data.res.model.impl.res.arsc.config.KeysHidden
import com.rosan.installer.data.res.model.impl.res.arsc.config.LayoutDir
import com.rosan.installer.data.res.model.impl.res.arsc.config.NavHidden
import com.rosan.installer.data.res.model.impl.res.arsc.config.Navigation
import com.rosan.installer.data.res.model.impl.res.arsc.config.Orientation
import com.rosan.installer.data.res.model.impl.res.arsc.config.ScreenLong
import com.rosan.installer.data.res.model.impl.res.arsc.config.ScreenRound
import com.rosan.installer.data.res.model.impl.res.arsc.config.ScreenSize
import com.rosan.installer.data.res.model.impl.res.arsc.config.Touchscreen
import com.rosan.installer.data.res.model.impl.res.arsc.config.UIModeNight
import com.rosan.installer.data.res.model.impl.res.arsc.config.UIModeType
import com.rosan.installer.data.res.model.impl.res.arsc.config.WideColorGamut
import java.util.*


data class TableConfig(
    val mcc: UShort,
    val mnc: UShort,
    // bytes's size must be 2
    val language: ByteArray,
    val country: ByteArray,
    val orientation: Orientation,
    val touchscreen: Touchscreen,
    val density: Int,
    val keyboard: Keyboard,
    val navigation: Navigation,
    val keysHidden: KeysHidden,
    val navHidden: NavHidden,
    val screenWidth: UShort,
    val screenHeight: UShort,
    val sdkVersion: UShort,
    val minorVersion: UShort,
    val screenLayout: UByte,
    val screenSize: ScreenSize,
    val screenLong: ScreenLong,
    val layoutDir: LayoutDir,
    val uiMode: UByte,
    val smallestScreenWidthDp: UShort,
    val screenWidthDp: UShort,
    val screenHeightDp: UShort,
    val localeScript: ByteArray,
    val localeVariant: ByteArray,
    val screenLayout2: UByte,
    val colorMode: UByte
) {
    val locale: Locale

    init {
        val language = unpackLanguage(language)
        val region = unpackRegion(country)
        val script = localeScript.decodeToString().substringBefore(Char(0x0))
        val variant = localeVariant.decodeToString().substringBefore(Char(0x0))
        var builder = Locale.Builder()
        kotlin.runCatching {
            if (language.isNotEmpty()) builder = builder.setLanguage(language)
            if (region.isNotEmpty()) builder = builder.setRegion(region)
            if (script.isNotEmpty()) builder = builder.setScript(script)
            if (variant.isNotEmpty()) builder = builder.setVariant(variant)
        }.exceptionOrNull()?.printStackTrace()
        locale = builder.build()
    }

    val isAnyScreenWidth = screenWidth == 0x0000u.toUShort()

    val isAnyScreenHeight = screenHeight == 0x0000u.toUShort()

    val isAnySdkVersion = sdkVersion == 0x0000u.toUShort()

    val isAnyMinorVersion = 0x0000u.toUShort()

    val isAnySmallestScreenWidthDp = smallestScreenWidthDp == 0x0000u.toUShort()

    val isAnyScreenWidthDp = screenWidthDp == 0x0000u.toUShort()

    val isAnyScreenHeightDp = screenHeightDp == 0x0000u.toUShort()

    val typedDensity = Density.build(density)

    val uiModeType = UIModeType.build(uiMode)

    val uiModeNight = UIModeNight.build(uiMode)

    val screenRound = ScreenRound.build(screenLayout2)

    val wideColorGamut = WideColorGamut.build(colorMode)

    val hdr = HDR.build(colorMode)

    companion object {
        fun unpackLanguage(language: ByteArray): String {
            return unpackLanguageOrRegion(language, 0x61)
        }

        fun unpackRegion(region: ByteArray): String {
            return unpackLanguageOrRegion(region, 0x30)
        }

        fun unpackLanguageOrRegion(bytes: ByteArray, base: Int): String {
            if (bytes.readByte(0).toInt() == 0
                && bytes.readByte(1).toInt() == 0
            ) {
                return ""
            }

            if (bytes.readByte(0).toInt() and 0x80 != 0) {
                byteArrayOf(
                    (base + (bytes.readByte(1).toInt() and 0x1F)).toByte(),
                    (base + ((bytes.readByte(1).toInt() and 0xE0) ushr 5) + ((bytes.readByte(0)
                        .toInt() and 0x03) shl 3)).toByte(),
                    (base + ((bytes.readByte(0).toInt() and 0x7C) ushr 2)).toByte()
                )
            }
            return bytes.readString(
                charset = Charsets.US_ASCII
            )
        }

        fun packLanguageOrRegion(str: String, base: Int): ByteArray {
            if (str.isEmpty()) return ByteArray(2) { 0 }
            val bytes = str.toByteArray(Charsets.US_ASCII)
            if (bytes.size == 2) return bytes
            return byteArrayOf(
                (((bytes.readByte(2) - base) shl 2) or ((bytes.readByte(1) - base) shr 3) or 0x80).toByte(),
                ((bytes.readByte(0) - base) or ((bytes.readByte(1) - base) shl 5)).toByte()
            )
        }

        fun build(repo: ReaderRepo): TableConfig {
            val reader = repo.readerRepo()
            val size = reader.int
            val mcc = reader.uShort
            val mnc = reader.uShort
            val language = reader.read(2)
            val country = reader.read(2)
            val orientation = Orientation.build(reader)
            val touchscreen = Touchscreen.build(reader)
            val density = reader.uShort.toInt()
            val keyboard = Keyboard.build(reader)
            val navigation = Navigation.build(reader)
            val keysHidden = KeysHidden.build(reader)
            val navHidden = NavHidden.build(reader)
            val screenWidth = reader.uShort
            val screenHeight = reader.uShort
            val sdkVersion = reader.uShort
            val minorVersion = reader.uShort

            var screenLayout: UByte = 0u
            var uiMode: UByte = 0u
            var smallestScreenWidthDp: UShort = 0u
            if (size >= 32) {
                screenLayout = reader.uByte
                uiMode = reader.uByte
                smallestScreenWidthDp = reader.uShort
            }
            val screenSize = ScreenSize.build(screenLayout)
            val screenLong = ScreenLong.build(screenLayout)
            val layoutDir = LayoutDir.build(screenLayout)

            var screenWidthDp: UShort = 0u
            var screenHeightDp: UShort = 0u
            if (size >= 36) {
                screenWidthDp = reader.uShort
                screenHeightDp = reader.uShort
            }

            val localeScript = ByteArray(4)
            val localeVariant = ByteArray(8)
            if (size >= 48) {
                reader.read(localeScript)
                reader.read(localeVariant)
            }

            var screenLayout2: UByte = 0u
            var colorMode: UByte = 0u
            var screenConfigPad2: UShort = 0u
            if (size >= 52) {
                screenLayout2 = reader.uByte
                colorMode = reader.uByte
                screenConfigPad2 = reader.uShort
            }

            if (size > reader.used) reader.skip(size - reader.used)
            return TableConfig(
                mcc,
                mnc,
                language,
                country,
                orientation,
                touchscreen,
                density,
                keyboard,
                navigation,
                keysHidden,
                navHidden,
                screenWidth,
                screenHeight,
                sdkVersion,
                minorVersion,
                screenLayout,
                screenSize,
                screenLong,
                layoutDir,
                uiMode,
                smallestScreenWidthDp,
                screenWidthDp,
                screenHeightDp,
                localeScript,
                localeVariant,
                screenLayout2,
                colorMode
            )
        }
    }
}

fun Locale.packLanguage(): ByteArray {
    return TableConfig.packLanguageOrRegion(this.language, 0x61)
}

fun Locale.packRegion(): ByteArray {
    return TableConfig.packLanguageOrRegion(this.country, 0x30)
}