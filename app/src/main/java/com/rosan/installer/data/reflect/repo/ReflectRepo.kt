package com.rosan.installer.data.reflect.repo

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

interface ReflectRepo {
    fun getConstructor(clazz: Class<*>, vararg parameterTypes: Class<*>): Constructor<*>?

    fun getDeclaredConstructor(clazz: Class<*>, vararg parameterTypes: Class<*>): Constructor<*>?

    fun getField(clazz: Class<*>, name: String): Field?

    fun getDeclaredField(clazz: Class<*>, name: String): Field?

    fun getMethod(clazz: Class<*>, name: String, vararg parameterTypes: Class<*>): Method?

    fun getDeclaredMethod(clazz: Class<*>, name: String, vararg parameterTypes: Class<*>): Method?
}