package com.rosan.installer.data.installer.model.impl.installer

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.system.Os
import androidx.annotation.RequiresApi
import com.hjq.permissions.XXPermissions
import com.rosan.installer.data.app.model.entity.*
import com.rosan.installer.data.app.model.impl.AnalyserRepoImpl
import com.rosan.installer.data.installer.model.entity.ProgressEntity
import com.rosan.installer.data.installer.model.entity.SelectInstallEntity
import com.rosan.installer.data.installer.model.entity.error.ResolveError
import com.rosan.installer.data.installer.model.impl.InstallerRepoImpl
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import com.rosan.installer.data.settings.util.ConfigUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.internal.closeQuietly
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class ActionHandler(
    worker: InstallerRepoImpl.MyWorker
) : Handler(worker), KoinComponent {
    private var job: Job? = null

    private val context by inject<Context>()

    private val cacheParcelFileDescriptors = mutableListOf<ParcelFileDescriptor>()

    private val cacheDirectory = "${context.externalCacheDir?.absolutePath}"

    private val cacheFiles = mutableListOf<String>()

    override suspend fun onStart() {
        job = worker.scope.launch {
            worker.impl.action.collect {
                // 异步处理请求
                launch {
                    when (it) {
                        is InstallerRepoImpl.Action.Resolve -> resolve(it.activity)
                        is InstallerRepoImpl.Action.Analyse -> analyse()
                        is InstallerRepoImpl.Action.Install -> install()
                        is InstallerRepoImpl.Action.Finish -> finish()
                    }
                }
            }
        }
    }

    override suspend fun onFinish() {
        cacheParcelFileDescriptors.forEach {
            it.closeQuietly()
        }
        cacheParcelFileDescriptors.clear()
        cacheFiles.forEach {
            File(it).delete()
        }
        cacheFiles.clear()
        job?.cancel()
    }

    private suspend fun resolve(activity: Activity) {
        worker.impl.progress.emit(ProgressEntity.Resolving)
        kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) requestNotificationPermission(
                activity
            )
            worker.impl.config = resolveConfig(activity)
        }.getOrElse {
            worker.impl.error = it
            worker.impl.progress.emit(ProgressEntity.ResolvedFailed)
            return
        }
        if (worker.impl.config.installMode == ConfigEntity.InstallMode.Ignore) {
            worker.impl.progress.emit(ProgressEntity.Finish)
            return
        }
        worker.impl.data = kotlin.runCatching {
            resolveData(activity)
        }.getOrElse {
            worker.impl.error = it
            worker.impl.progress.emit(ProgressEntity.ResolvedFailed)
            return
        }
        worker.impl.progress.emit(ProgressEntity.ResolveSuccess)
    }

    private suspend fun resolveConfig(activity: Activity): ConfigEntity {
        val packageName = activity.callingPackage
            ?: (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) activity.referrer?.host else null)
        var config = ConfigUtil.getByPackageName(packageName)
        if (config.installer == null) config = config.copy(
            installer = packageName
        )
        return config
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun requestNotificationPermission(activity: Activity) {
        callbackFlow<Any?> {
            val permissions = listOf(Manifest.permission.POST_NOTIFICATIONS)
            if (XXPermissions.isGranted(activity, permissions)) {
                send(null)
            } else {
                XXPermissions.with(activity).permission(permissions).request { _, all ->
                    if (all) trySend(null)
                    else close()
                }
            }
            awaitClose { }
        }.first()
    }

    private suspend fun resolveData(activity: Activity): List<DataEntity> {
        requestStoragePermissions(activity)
        val uris = resolveDataUris(activity)
        val data = mutableListOf<DataEntity>()
        uris.forEach {
            data.addAll(resolveDataUri(activity, it))
        }
        return data
    }

    private suspend fun requestStoragePermissions(activity: Activity) {
        callbackFlow<Any?> {
            val permissions = listOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            if (XXPermissions.isGranted(activity, permissions)) {
                send(null)
            } else {
                XXPermissions.with(activity).permission(permissions).request { _, all ->
                    if (all) trySend(null)
                    else close()
                }
            }
            awaitClose { }
        }.first()
    }

    private fun resolveDataUris(activity: Activity): List<Uri> {
        val intent = activity.intent ?: throw ResolveError(
            action = null, uris = emptyList()
        )
        val intentAction = intent.action ?: throw ResolveError(
            action = null, uris = emptyList()
        )

        val uris = when (intentAction) {
            Intent.ACTION_SEND -> {
                val uri =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent.getParcelableExtra(
                        Intent.EXTRA_STREAM, Uri::class.java
                    )
                    else intent.getParcelableExtra(Intent.EXTRA_STREAM)
                if (uri == null) emptyList() else listOf(uri)
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent.getParcelableArrayListExtra(
                    Intent.EXTRA_STREAM, Uri::class.java
                )
                else intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)) ?: emptyList()
            }

            else -> {
                val uri = intent.data
                if (uri == null) emptyList()
                else listOf(uri)
            }
        }

        if (uris.isEmpty()) throw ResolveError(
            action = intentAction, uris = uris
        )
        return uris
    }

    private fun resolveDataUri(activity: Activity, uri: Uri): List<DataEntity> {
        if (uri.scheme == ContentResolver.SCHEME_FILE) return resolveDataFileUri(activity, uri)
        return resolveDataContentFile(activity, uri)
    }

    private fun resolveDataFileUri(activity: Activity, uri: Uri): List<DataEntity> {
        val path = uri.path ?: throw Exception("can't get uri path: $uri")
        val data = DataEntity.FileEntity(path)
        data.source = DataEntity.FileEntity(path)
        return listOf(data)
    }

    private fun resolveDataContentFile(
        activity: Activity,
        uri: Uri,
        retry: Int = 3
    ): List<DataEntity> {
        val retry = retry - 1
        // wait for PermissionRecords ok.
        // if not, maybe show Uri Read Permission Denied
        if (activity.checkCallingOrSelfUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            ) != PackageManager.PERMISSION_GRANTED &&
            retry > 0
        ) {
            Thread.sleep(50)
            return resolveDataContentFile(activity, uri, retry - 1)
        }
        val assetFileDescriptor = activity.contentResolver?.openAssetFileDescriptor(uri, "r")
            ?: throw Exception("can't open file descriptor: $uri")
        val parcelFileDescriptor = assetFileDescriptor.parcelFileDescriptor
        val pid = Os.getpid()
        val descriptor = parcelFileDescriptor.fd
//        val path = "/proc/$pid/task/${Os.gettid()}/fd/$descriptor"
        val path = "/proc/$pid/fd/$descriptor"

        // only full file, can't handle a sub-section of a file
        if (assetFileDescriptor.declaredLength < 0) {

            // file descriptor can't be pipe or socket
            val source = Os.readlink(path)
            if (source.startsWith('/')) {
                cacheParcelFileDescriptors.add(parcelFileDescriptor)
                val file = File(path)
                val data = if (file.exists() && file.canRead() && kotlin.runCatching {
                        file.inputStream().use { }
                        return@runCatching true
                    }.getOrDefault(false)) DataEntity.FileEntity(path)
                else DataEntity.FileDescriptorEntity(pid, descriptor)
                data.source = DataEntity.FileEntity(source)
                return listOf(data)
            }
        }

        // cache it
        val tempFile = File.createTempFile(worker.impl.id, null, File(cacheDirectory))
        tempFile.outputStream().use {
            assetFileDescriptor.createInputStream().copyTo(it)
        }
        assetFileDescriptor.closeQuietly()
        cacheFiles.add(tempFile.absolutePath)
        return listOf(DataEntity.FileEntity(tempFile.absolutePath))
    }

    private suspend fun analyse() {
        worker.impl.progress.emit(ProgressEntity.Analysing)
        worker.impl.entities = kotlin.runCatching {
            analyseEntities(worker.impl.data)
        }.getOrElse {
            worker.impl.error = it
            worker.impl.progress.emit(ProgressEntity.AnalysedFailed)
            return
        }.sortedWith(compareBy({
            it.packageName
        }, {
            when (it) {
                is AppEntity.BaseEntity -> it.name
                is AppEntity.SplitEntity -> it.name
                is AppEntity.DexMetadataEntity -> it.name
            }
        })).map {
            SelectInstallEntity(
                app = it, selected = true
            )
        }
        worker.impl.progress.emit(ProgressEntity.AnalysedSuccess)
    }

    private suspend fun analyseEntities(data: List<DataEntity>): List<AppEntity> =
        AnalyserRepoImpl().doWork(worker.impl.config, data)

    private suspend fun install() {
        worker.impl.progress.emit(ProgressEntity.Installing)
        kotlin.runCatching {
            installEntities(worker.impl.config, worker.impl.entities.filter { it.selected }.map {
                InstallEntity(
                    name = it.app.name,
                    packageName = it.app.packageName,
                    data = when (val app = it.app) {
                        is AppEntity.BaseEntity -> app.data
                        is AppEntity.SplitEntity -> app.data
                        is AppEntity.DexMetadataEntity -> app.data
                    }
                )
            }, InstallExtraEntity(Os.getuid() / 100000)
            )
        }.getOrElse {
            worker.impl.error = it
            worker.impl.progress.emit(ProgressEntity.InstallFailed)
            return
        }
        worker.impl.progress.emit(ProgressEntity.InstallSuccess)
    }

    private suspend fun installEntities(
        config: ConfigEntity, entities: List<InstallEntity>, extra: InstallExtraEntity
    ) = com.rosan.installer.data.app.model.impl.InstallerRepoImpl.doWork(
        config, entities, extra
    )

    private suspend fun finish() {
        worker.impl.progress.emit(ProgressEntity.Finish)
    }
}