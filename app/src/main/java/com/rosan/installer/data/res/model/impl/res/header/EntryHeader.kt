package com.rosan.installer.data.res.model.impl.res.header

import com.rosan.installer.data.io.repo.ReaderRepo

data class EntryHeader(
    val size: Short,
    val flags: Flags,
    val key: Int
) {
    val length = 8

    val chunkSize = size

    data class Flags(val value: UShort) {
        val isComplex = value and 0x0001u != 0u.toUShort()

        val isPublic = value and 0x0002u != 0u.toUShort()

        companion object {
            fun build(repo: ReaderRepo): Flags {
                return build(repo.uShort)
            }

            fun build(value: UShort): Flags {
                return Flags(value)
            }
        }
    }

    companion object {
        fun build(repo: ReaderRepo): EntryHeader {
            return EntryHeader(
                repo.short,
                Flags.build(repo),
                repo.int
            )
        }
    }
}