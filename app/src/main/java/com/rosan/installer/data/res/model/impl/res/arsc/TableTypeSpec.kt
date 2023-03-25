package com.rosan.installer.data.res.model.impl.res.arsc

import com.rosan.installer.data.io.model.impl.readerRepo
import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.impl.res.arsc.header.TableTypeSpecHeader

data class TableTypeSpec(
    val id: Int,
    val entries: List<Configs>
) {
    class Configs(val value: UInt) {
        val hasMCC = isConfig(0x0001u)

        val hasMNC = isConfig(0x0002u)

        val hasLocale = isConfig(0x0004u)

        val hasTouchscreen = isConfig(0x0008u)

        val hasKeyboard = isConfig(0x0010u)

        val hasKeyboardHidden = isConfig(0x0020u)

        val hasNavigation = isConfig(0x0040u)

        val hasOrientation = isConfig(0x0080u)

        val hasDensity = isConfig(0x0100u)

        val hasScreenSize = isConfig(0x0200u)

        val hasVersion = isConfig(0x0400u)

        val hasScreenLayout = isConfig(0x0800u)

        val hasUIMode = isConfig(0x1000u)

        val hasSmallestScreenSize = isConfig(0x2000u)

        val hasLayoutDir = isConfig(0x4000u)

        val hasScreenRound = isConfig(0x8000u)

        val hasColorMode = isConfig(0x10000u)

        fun isConfig(config: UInt): Boolean {
            return value and config != 0u
        }

        companion object {
            fun build(repo: ReaderRepo): Configs {
                return build(repo.uInt)
            }

            fun build(value: UInt): Configs {
                return Configs(value)
            }
        }
    }

    companion object {
        fun build(repo: ReaderRepo, header: TableTypeSpecHeader): TableTypeSpec {
            val reader = repo.readerRepo()

            val entries = mutableListOf<Configs>()
            for (i in 0 until header.entryCount)
                entries.add(Configs.build(reader))
            if (header.chunkSize > header.length + reader.used) reader.skip(header.chunkSize - header.length - reader.used)
            return TableTypeSpec(
                header.id,
                entries
            )
        }
    }
}