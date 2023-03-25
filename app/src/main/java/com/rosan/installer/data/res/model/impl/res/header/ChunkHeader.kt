package com.rosan.installer.data.res.model.impl.res.header

import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.entity.error.ChunkTypeUnknownError
import kotlinx.serialization.Serializable

class ChunkHeader(
    val type: Type,
    val headerSize: Int,
    override val chunkSize: Int
) : BaseHeader {
    override val length: Int = 8

    @Serializable
    enum class Type(val value: UShort) {
        NULL(0x0000u),
        STRING_POOL(0x0001u),
        TABLE(0x0002u),
        XML(0x0003u),

        XML_START_NAMESPACE(0x0100u),
        XML_END_NAMESPACE(0x0101u),
        XML_START_ELEMENT(0x0102u),
        XML_END_ELEMENT(0x0103u),
        XML_CDATA(0x0104u),

        XML_RESOURCE_MAP(0x0180u),

        TABLE_PACKAGE(0x0200u),
        TABLE_TYPE(0x0201u),
        TABLE_TYPE_SPEC(0x0202u),
        TABLE_LIBRARY(0x0203u),
        TABLE_OVERLAYABLE(0x0204u),
        TABLE_OVERLAYABLE_POLICY(0x0205u),
        TABLE_STAGED_ALIAS(0x0206u);

        companion object {
            const val XML_FIRST_CHUNK: UShort = 0x0100u

            const val XML_LAST_CHUNK: UShort = 0x017fu

            fun build(repo: ReaderRepo): Type {
                return build(repo.uShort)
            }

            fun build(value: UShort): Type {
                return Type.values().find { it.value == value } ?: throw ChunkTypeUnknownError(
                    value
                )
            }
        }
    }

    companion object {
        fun build(repo: ReaderRepo): ChunkHeader {
            val type = Type.build(repo)
            val headerSize = repo.uShort.toInt()
            val chunkSize = repo.int
            return ChunkHeader(type, headerSize, chunkSize)
        }
    }
}
