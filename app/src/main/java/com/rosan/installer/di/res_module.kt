package com.rosan.installer.di

import android.content.res.XmlResourceParser
import com.rosan.installer.data.res.model.impl.ArscRepoImpl
import com.rosan.installer.data.res.model.impl.AxmlPullRepoImpl
import com.rosan.installer.data.res.model.impl.AxmlTreeRepoImpl
import com.rosan.installer.data.res.repo.ArscRepo
import com.rosan.installer.data.res.repo.AxmlPullRepo
import com.rosan.installer.data.res.repo.AxmlTreeRepo
import org.koin.dsl.module

val resModule = module {
    factory<AxmlPullRepo> {
        AxmlPullRepoImpl(get())
    }

    factory<AxmlTreeRepo> {
        val xmlPull = get<XmlResourceParser>()
        val path = getOrNull<String>()
        if (path == null) {
            AxmlTreeRepoImpl(xmlPull)
        } else {
            AxmlTreeRepoImpl(get(), path)
        }
    }

    factory<ArscRepo> {
        ArscRepoImpl(get())
    }
}