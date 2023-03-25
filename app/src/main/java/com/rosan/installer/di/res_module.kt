package com.rosan.installer.di

import android.content.res.XmlResourceParser
import com.rosan.installer.data.res.model.impl.AxmlTreeRepoImpl
import com.rosan.installer.data.res.repo.AxmlTreeRepo
import org.koin.dsl.module

val resModule = module {
    factory<AxmlTreeRepo> {
        val xmlPull = get<XmlResourceParser>()
        val path = getOrNull<String>()
        if (path == null) {
            AxmlTreeRepoImpl(xmlPull)
        } else {
            AxmlTreeRepoImpl(get(), path)
        }
    }
}