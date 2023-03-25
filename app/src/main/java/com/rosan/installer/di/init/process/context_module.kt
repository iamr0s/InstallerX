package com.rosan.installer.di.init.process

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.rosan.installer.data.reflect.repo.ReflectRepo
import org.koin.dsl.module

@SuppressLint("PrivateApi")
val contextModule = module {
    single<Context?> {
        Looper.prepare()
        val reflect: ReflectRepo = get()
        val activityThreadClz = Class.forName("android.app.ActivityThread")
        var activityThread = reflect.getDeclaredMethod(
            activityThreadClz,
            "currentActivityThread"
        )!!.let {
            it.isAccessible = true
            it.invoke(null)
        }
        if (activityThread == null) {
            activityThread = reflect.getDeclaredConstructor(
                activityThreadClz
            )!!.let {
                it.isAccessible = true
                it.newInstance()
            }
        }
        val context = reflect.getDeclaredMethod(
            activityThreadClz,
            "getSystemContext"
        )!!.let {
            it.isAccessible = true
            it.invoke(activityThread)
        } as Context
        context
    }
}