package com.rosan.installer.data.app.repo.analyser

import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.rosan.installer.data.app.model.entity.AnalyseEntity
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.repo.AnalyserRepo
import com.rosan.installer.data.common.model.entity.ErrorEntity
import com.rosan.installer.data.res.repo.AxmlTreeRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

interface AManifestApkAnalyserRepo : AnalyserRepo, KoinComponent {
    suspend fun loadAppEntity(
        entity: AnalyseEntity,
        resources: Resources,
        theme: Resources.Theme?,
        parser: XmlResourceParser,
    ): AppEntity = withContext(Dispatchers.IO) {
        var packageName: String? = null
        var splitName: String? = null
        var versionCode: Long = -1
        var versionName = ""
        var label: String? = null
        var icon: Drawable? = null
        var roundIcon: Drawable? = null
        get<AxmlTreeRepo> {
            parametersOf(parser)
        }.register("/manifest") {
            packageName = getAttributeValue(null, "package") ?: packageName
            splitName = getAttributeValue(null, "split") ?: splitName
            versionCode = getAttributeIntValue(
                AxmlTreeRepo.ANDROID_NAMESPACE, "versionCode", versionCode.toInt()
            ).toLong()
            versionName =
                getAttributeValue(AxmlTreeRepo.ANDROID_NAMESPACE, "versionName") ?: versionName
        }.register("/manifest/application") {
            let {
                label = when (val resId =
                    getAttributeResourceValue(AxmlTreeRepo.ANDROID_NAMESPACE, "label", -1)) {
                    -1 -> getAttributeValue(AxmlTreeRepo.ANDROID_NAMESPACE, "label") ?: label
                    0 -> null
                    else -> resources.getString(resId)
                }
            }
            let {
                icon = when (val resId =
                    getAttributeResourceValue(AxmlTreeRepo.ANDROID_NAMESPACE, "icon", -1)) {
                    -1 -> null
                    0 -> null
                    else -> ResourcesCompat.getDrawable(resources, resId, theme)
                }
            }
            let {
                roundIcon = when (val resId = getAttributeResourceValue(
                    AxmlTreeRepo.ANDROID_NAMESPACE, "roundIcon", -1
                )) {
                    -1 -> null
                    0 -> null
                    else -> ResourcesCompat.getDrawable(resources, resId, theme)
                }
            }
        }.map {
        }
        if (packageName.isNullOrEmpty()) throw ErrorEntity("not android manifest")
        return@withContext if (splitName.isNullOrEmpty()) AppEntity.MainEntity(
            packageName = packageName!!,
            versionCode = versionCode,
            versionName = versionName,
            label = label,
            icon = roundIcon ?: icon,
            data = entity.data
        )
        else AppEntity.SplitEntity(
            packageName = packageName!!,
            splitName = splitName!!,
            data = entity.data
        )
    }
}