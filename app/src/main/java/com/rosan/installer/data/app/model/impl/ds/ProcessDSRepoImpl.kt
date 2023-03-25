package com.rosan.installer.data.app.model.impl.ds

import android.content.ComponentName
import android.content.ContentResolver
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.IPackageManager
import android.content.pm.PackageManager
import android.content.pm.ParceledListSlice
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.ServiceManager
import com.rosan.installer.data.app.repo.DSRepo
import com.rosan.installer.data.reflect.repo.ReflectRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ProcessDSRepoImpl : DSRepo, KoinComponent {
    override suspend fun doWork(
        config: ConfigEntity, packageName: String, className: String, enabled: Boolean
    ) {
        val uid = android.os.Process.myUid()
        val userId = uid / 100000
        val iPackageManager = IPackageManager.Stub.asInterface(ServiceManager.getService("package"))

        // 查询支持列表
        val intent = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_DEFAULT)
            .setDataAndType(
                Uri.parse("content://storage/emulated/0/test.apk"),
                "application/vnd.android.package-archive"
            )
        val list = queryIntentActivities(
            iPackageManager,
            intent,
            "application/vnd.android.package-archive",
            PackageManager.MATCH_DEFAULT_ONLY,
            userId
        )
        var bestMatch = 0
        val names = list.map {
            val iPackageName = it.activityInfo.packageName
            val iClassName = it.activityInfo.name

            if (it.match > bestMatch) bestMatch = it.match

            // clear preferred
            iPackageManager.clearPackagePreferredActivities(iPackageName)
            if (uid == 1000) iPackageManager.clearPackagePersistentPreferredActivities(
                iPackageName,
                userId
            )

            ComponentName(iPackageName, iClassName)
        }.toTypedArray()

        if (!enabled) {
            iPackageManager.flushPackageRestrictionsAsUser(userId)
            return
        }

        val filter = IntentFilter(Intent.ACTION_VIEW)
        filter.addAction(Intent.ACTION_MAIN)
        filter.addAction(Intent.CATEGORY_HOME)
        filter.addAction(Intent.ACTION_INSTALL_PACKAGE)
        filter.addCategory(Intent.CATEGORY_DEFAULT)
        filter.addDataScheme(ContentResolver.SCHEME_CONTENT)
        filter.addDataScheme(ContentResolver.SCHEME_FILE)
        filter.addDataType("application/vnd.android.package-archive")
        println("preferring: $packageName")
        val componentName = ComponentName(packageName, className)
        iPackageManager.setLastChosenActivity(
            intent,
            intent.type,
            PackageManager.MATCH_DEFAULT_ONLY,
            filter,
            bestMatch,
            componentName
        )
        addPreferredActivity(
            iPackageManager,
            filter,
            bestMatch,
            names,
            componentName,
            userId,
            true
        )
        if (uid == 1000) addPersistentPreferredActivity(
            iPackageManager,
            filter,
            componentName,
            userId
        )
        iPackageManager.flushPackageRestrictionsAsUser(userId)
        println("preferred: $packageName")
    }

    private fun addPreferredActivity(
        iPackageManager: IPackageManager,
        filter: IntentFilter,
        match: Int,
        names: Array<ComponentName>,
        name: ComponentName,
        userId: Int,
        removeExisting: Boolean
    ) {
        val repo = get<ReflectRepo>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            repo.getDeclaredMethod(
                IPackageManager::class.java,
                "addPreferredActivity",
                IntentFilter::class.java,
                Int::class.java,
                Array<ComponentName>::class.java,
                ComponentName::class.java,
                Int::class.java,
                Boolean::class.java,
            )?.invoke(
                iPackageManager,
                filter,
                match,
                names,
                name,
                userId,
                removeExisting
            )
        } else {
            repo.getDeclaredMethod(
                IPackageManager::class.java,
                "addPreferredActivity",
                IntentFilter::class.java,
                Int::class.java,
                Array<ComponentName>::class.java,
                ComponentName::class.java,
                Int::class.java
            )?.invoke(
                iPackageManager,
                filter,
                match,
                names,
                name,
                userId
            )
        }
    }

    private fun addPersistentPreferredActivity(
        iPackageManager: IPackageManager,
        filter: IntentFilter,
        name: ComponentName,
        userId: Int,
    ) {
        val repo = get<ReflectRepo>()
        repo.getDeclaredMethod(
            IPackageManager::class.java,
            "addPersistentPreferredActivity",
            IntentFilter::class.java,
            ComponentName::class.java,
            Int::class.java,
        )?.invoke(
            iPackageManager,
            filter,
            name,
            userId,
        )
    }

    private fun queryIntentActivities(
        iPackageManager: IPackageManager,
        intent: Intent,
        resolvedType: String,
        flags: Int,
        userId: Int
    ): List<ResolveInfo> {
        val repo = get<ReflectRepo>()
        return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            repo.getDeclaredMethod(
                IPackageManager::class.java,
                "queryIntentActivities",
                Intent::class.java,
                String::class.java,
                Long::class.java,
                Int::class.java
            )?.invoke(iPackageManager, intent, resolvedType, flags.toLong(), userId)
        } else {
            repo.getDeclaredMethod(
                IPackageManager::class.java,
                "queryIntentActivities",
                Intent::class.java,
                String::class.java,
                Int::class.java,
                Int::class.java
            )?.invoke(iPackageManager, intent, resolvedType, flags, userId)
        } as ParceledListSlice<ResolveInfo>).list
    }
}