package com.rosan.installer.data.recycle.model.impl

object AppProcessPrivilegedServiceRecyclers {
    private val map =
        mutableMapOf<String, AppProcessPrivilegedServiceRecycler>()

    fun get(shell: String): AppProcessPrivilegedServiceRecycler {
        return map.getOrPut(shell) {
            AppProcessPrivilegedServiceRecycler(shell)
        }
    }
}