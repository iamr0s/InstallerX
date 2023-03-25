package com.rosan.installer.data.res.model.impl.res.axml

import com.rosan.installer.data.io.repo.ReaderRepo
import com.rosan.installer.data.res.model.impl.res.Value

data class XMLAttribute(
    val namespace: Int,
    val name: Int,
    val rawValue: Int,
    val typedValue: Value
) {
    companion object {
        fun build(repo: ReaderRepo): XMLAttribute {
            return XMLAttribute(
                repo.int,
                repo.int,
                repo.int,
                Value.build(repo)
            )
        }
    }
}