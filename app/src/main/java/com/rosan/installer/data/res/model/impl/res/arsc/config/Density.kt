package com.rosan.installer.data.res.model.impl.res.arsc.config

import com.rosan.installer.data.io.repo.ReaderRepo

enum class Density(val value: Int) {
    Default(0),
    Low(120),
    Medium(160),
    TV(213),
    High(240),
    XHigh(320),
    XXHigh(480),
    XXXHigh(640),
    Any(0xfffe),
    None(0xffff);

    companion object {
        fun build(repo: ReaderRepo): Density {
            return build(repo.uShort.toInt())
        }

        fun build(value: Int): Density {
            return values().find { it.value == value }
                ?: Medium
        }
    }
}