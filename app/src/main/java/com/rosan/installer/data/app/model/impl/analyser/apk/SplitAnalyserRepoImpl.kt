package com.rosan.installer.data.app.model.impl.analyser.apk

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import android.content.res.ApkAssets
import android.content.res.AssetManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.rosan.installer.data.app.model.entity.AnalyseEntity
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.app.model.entity.error.DataEntityNotSupportError
import com.rosan.installer.data.app.repo.analyser.AManifestApkAnalyserRepo
import com.rosan.installer.data.common.model.entity.ErrorEntity
import com.rosan.installer.data.reflect.repo.ReflectRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import java.lang.reflect.Modifier
import java.util.zip.ZipFile

class SplitAnalyserRepoImpl : AManifestApkAnalyserRepo, KoinComponent {
    private val context by inject<Context>()

    /*
    * Fix resource hook on MediaTek
    * @url https://github.com/LSPosed/LSPosed/pull/1951
    *
    * 联发科的bug，在新系统已经解决了这个问题，但是有一些旧机型得不到更新
    * 可以手动执行`resetprop ro.vendor.pref_scale_enable_cfg 0`解决
    * @url https://github.com/LSPosed/LSPosed/commit/cbd19d17a81e1c87c20cb6ee8d3231964b35e1b6
    * */
    @SuppressLint("PrivateApi")
    private fun fixResourceHookOnMediaTek() {
        val contextField = get<ReflectRepo>().getDeclaredField(
            Class.forName("android.content.res.ResourcesImpl"),
            "mAppContext"
        ) ?: return
        if (!Modifier.isStatic(contextField.modifiers)) return
        contextField.isAccessible = true

        val context = object : ContextWrapper(null) {
            override fun getApplicationInfo(): ApplicationInfo {
//                return super.getApplicationInfo()
                return ApplicationInfo().also {
                    it.processName = "system"
                }
            }
        }
        contextField.set(null, context)
    }

    init {
//        fixResourceHookOnMediaTek()
    }

    /**
     * Resources.getAssets().addAssetPath
     * 从而得到对应Apk的Resources
     * */
    override suspend fun doWork(
        config: ConfigEntity,
        entities: List<AnalyseEntity>,
    ): List<AppEntity> = withContext(Dispatchers.IO) {
        val apps = mutableListOf<AppEntity>()
        entities.forEach { entity ->
            if (entity.data !is DataEntity.FileEntity) throw DataEntityNotSupportError(entity.data)
            val path = entity.data.path
            ZipFile(path).use {
                if (it.getEntry("AndroidManifest.xml") == null) throw ErrorEntity("not android package")
            }
            val resources = context.resources
            val assetManager = resources.assets

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                setAssetPath(assetManager, path)
            } else {
                addAssetPath(assetManager, path)
            }

            apps.add(
                loadAppEntity(
                    entity,
                    resources,
                    resources.newTheme(),
                    assetManager.openXmlResourceParser("AndroidManifest.xml")
                )
            )
        }
        return@withContext apps
    }

    private fun addAssetPath(assetManager: AssetManager, path: String) {
        val cookie = get<ReflectRepo>().getDeclaredMethod(
            AssetManager::class.java,
            "addAssetPath",
            String::class.java
        )!!.let {
            it.isAccessible = true
            it.invoke(assetManager, path)
        } as Int
        if (cookie == 0) throw ErrorEntity("the cookie of the added asset, or 0 on failure.")
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private fun setAssetPath(assetManager: AssetManager, path: String) {
        get<ReflectRepo>().getDeclaredMethod(
            AssetManager::class.java,
            "setApkAssets",
            Array<ApkAssets>::class.java,
            Boolean::class.java
        )!!.let {
            it.isAccessible = true
            it.invoke(
                assetManager,
                arrayOf(ApkAssets.loadFromPath(path)),
                true
            )
        }
    }
}