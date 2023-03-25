package com.rosan.installer.data.reflect.model.impl

import com.rosan.installer.data.reflect.repo.ReflectRepo
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

class ReflectRepoImpl : ReflectRepo {
    override fun getConstructor(clazz: Class<*>, vararg parameterTypes: Class<*>): Constructor<*>? {
        for (constructor in clazz.constructors) {
            val expectedTypes = constructor.parameterTypes
            if (expectedTypes.size != parameterTypes.size) continue
            for (i in expectedTypes.indices)
                if (expectedTypes[i] != parameterTypes[i]) continue
            return constructor
        }
        return null
    }

    override fun getDeclaredConstructor(
        clazz: Class<*>,
        vararg parameterTypes: Class<*>
    ): Constructor<*>? {
        for (constructor in clazz.declaredConstructors) {
            val expectedTypes = constructor.parameterTypes
            if (expectedTypes.size != parameterTypes.size) continue
            for (i in expectedTypes.indices)
                if (expectedTypes[i] != parameterTypes[i]) continue
            return constructor
        }
        return null
    }

    override fun getField(clazz: Class<*>, name: String): Field? {
        for (field in clazz.fields) {
            if (field.name != name) continue
            return field
        }
        return null
    }

    override fun getDeclaredField(clazz: Class<*>, name: String): Field? {
        for (field in clazz.declaredFields) {
            if (field.name != name) continue
            return field
        }
        return null
    }

    override fun getMethod(
        clazz: Class<*>,
        name: String,
        vararg parameterTypes: Class<*>
    ): Method? {
        for (method in clazz.methods) {
            if (method.name != name) continue
            val expectedTypes = method.parameterTypes
            if (expectedTypes.size != parameterTypes.size) continue
            for (i in expectedTypes.indices)
                if (expectedTypes[i] != parameterTypes[i]) continue
            return method
        }
        return null
    }

    override fun getDeclaredMethod(
        clazz: Class<*>,
        name: String,
        vararg parameterTypes: Class<*>
    ): Method? {
        for (method in clazz.declaredMethods) {
            if (method.name != name) continue
            val expectedTypes = method.parameterTypes
            if (expectedTypes.size != parameterTypes.size) continue
            for (i in expectedTypes.indices)
                if (expectedTypes[i] != parameterTypes[i]) continue
            return method
        }
        return null
    }
}