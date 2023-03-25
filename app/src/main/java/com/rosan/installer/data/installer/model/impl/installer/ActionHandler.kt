package com.rosan.installer.data.installer.model.impl.installer

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.system.Os
import androidx.annotation.RequiresApi
import com.hjq.permissions.XXPermissions
import com.rosan.installer.data.app.model.entity.AnalyseEntity
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.app.model.entity.InstallExtraEntity
import com.rosan.installer.data.app.model.impl.AnalyserRepoImpl
import com.rosan.installer.data.app.model.impl.installer.AuthorizerInstallerRepoImpl
import com.rosan.installer.data.app.util.DataType
import com.rosan.installer.data.installer.model.entity.InstallEntity
import com.rosan.installer.data.installer.model.entity.ProgressEntity
import com.rosan.installer.data.installer.model.entity.error.ResolveError
import com.rosan.installer.data.installer.model.impl.InstallerRepoImpl
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import com.rosan.installer.data.settings.util.ConfigUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class ActionHandler(
    worker: InstallerRepoImpl.MyWorker
) : Handler(worker), KoinComponent {
    private var job: Job? = null

    private val context by inject<Context>()

    private val cachePath = "${context.externalCacheDir?.absolutePath}/${worker.impl.id}".also {
        val file = File(it)
        if (!file.exists()) file.mkdirs()
    }

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

    private fun deleteFile(file: File) {
        if (!file.exists()) return
        if (file.isDirectory) file.listFiles()?.forEach {
            deleteFile(it)
        }
        file.delete()
    }

    override suspend fun onFinish() {
        deleteFile(File(cachePath))
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun requestNotificationPermission(activity: Activity) {
        callbackFlow<Any?> {
            val permissions = listOf(Manifest.permission.POST_NOTIFICATIONS)
            if (XXPermissions.isGranted(activity, permissions)) {
                send(null)
            } else {
                XXPermissions.with(activity)
                    .permission(permissions)
                    .request { _, all ->
                        if (all) trySend(null)
                        else close()
                    }
            }
            awaitClose { }
        }.first()
    }

    private suspend fun resolveConfig(activity: Activity): ConfigEntity {
        val packageName =
            activity.callingPackage
                ?: (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                    activity.referrer?.host else null)
        return ConfigUtil.getByPackageName(packageName)
    }

    private suspend fun resolveData(activity: Activity): List<DataEntity> {
        requestStoragePermissions(activity)
        val uris = resolveDataUris(activity)
        if (!worker.impl.config.compatMode) return resolveOriginData(activity, uris)
        return resolveCopyData(activity, uris)
    }

    private suspend fun requestStoragePermissions(activity: Activity) {
        callbackFlow<Any?> {
            val permissions = listOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            if (XXPermissions.isGranted(activity, permissions)) {
                send(null)
            } else {
                XXPermissions.with(activity)
                    .permission(permissions)
                    .request { _, all ->
                        if (all) trySend(null)
                        else close()
                    }
            }
            awaitClose { }
        }.first()
    }

    private fun resolveDataUris(activity: Activity): List<Uri> {
        val intent = activity.intent ?: throw ResolveError(
            action = null,
            uris = emptyList()
        )
        val intentAction = intent.action ?: throw ResolveError(
            action = null,
            uris = emptyList()
        )

        val uris = when (intentAction) {
            Intent.ACTION_SEND -> {
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                else intent.getParcelableExtra(Intent.EXTRA_STREAM)
                if (uri == null) emptyList() else listOf(uri)
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                else intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM))
                    ?: emptyList()
            }
            else -> {
                val uri = intent.data
                if (uri == null) emptyList()
                else listOf(uri)
            }
        }

        if (uris.isEmpty()) throw ResolveError(
            action = intentAction,
            uris = uris
        )
        return uris
    }

    private fun resolveOriginData(activity: Activity, uris: List<Uri>): List<DataEntity> {
        return uris.map {
            val path = when (it.scheme) {
                ContentResolver.SCHEME_FILE -> it.path
                ContentResolver.SCHEME_CONTENT -> {
                    activity.contentResolver?.openFileDescriptor(it, "r")?.use {
                        val fd = "/proc/self/fd/${it.fd}"
                        val path = Os.readlink(fd)
                        if (path != fd) path else null
                    }
                }
                else -> null
            } ?: throw Error("bad uri: $it")
            DataEntity.FileEntity(path)
        }
    }

    private fun resolveCopyData(activity: Activity, uris: List<Uri>): List<DataEntity> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uris.map {
                when (it.scheme) {
                    ContentResolver.SCHEME_CONTENT -> {
                        activity.contentResolver.openInputStream(it)?.use { inputStream ->
                            val path =
                                "$cachePath/${System.currentTimeMillis()}-${it.path?.let { File(it).name }}"
                            File(path).outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                            DataEntity.FileEntity(path)
                        }
                    }
                    ContentResolver.SCHEME_FILE -> {
                        val path = it.path ?: throw Error(
                            "bad uri: $it"
                        )
                        DataEntity.FileEntity(path)
                    }
                    else -> null
                } ?: throw Error("bad uri: $it")
            }
        } else {
            uris.map {
                val path = it.path
                if (
                    (it.scheme != ContentResolver.SCHEME_CONTENT && it.scheme != ContentResolver.SCHEME_FILE)
                    || path == null
                ) throw Error(
                    "bad uri: $it"
                )
                DataEntity.FileEntity(path)
            }
        }
    }

    private suspend fun analyse() {
        worker.impl.progress.emit(ProgressEntity.Analysing)
        worker.impl.entities = kotlin.runCatching {
            analyseEntities(worker.impl.data)
        }.getOrElse {
            worker.impl.error = it
            worker.impl.progress.emit(ProgressEntity.AnalysedFailed)
            return
        }.sortedWith(compareBy(
            {
                it.packageName
            },
            {
                when (it) {
                    is AppEntity.MainEntity -> ""
                    is AppEntity.SplitEntity -> it.splitName
                }
            }
        )).map {
            InstallEntity(
                app = it,
                selected = true
            )
        }
        worker.impl.progress.emit(ProgressEntity.AnalysedSuccess)
    }

    private suspend fun analyseEntities(data: List<DataEntity>): List<AppEntity> =
        AnalyserRepoImpl().doWork(worker.impl.config, data.map {
            AnalyseEntity(it, DataType.AUTO)
        })

    private suspend fun install() {
        worker.impl.progress.emit(ProgressEntity.Installing)
        kotlin.runCatching {
            installEntities(worker.impl.config, worker.impl.entities.filter {
                it.selected
            }.map {
                it.app
            }, InstallExtraEntity(android.os.Process.myUid() / 100000))
        }.getOrElse {
            worker.impl.error = it
            worker.impl.progress.emit(ProgressEntity.InstallFailed)
            return
        }
        worker.impl.progress.emit(ProgressEntity.InstallSuccess)
    }

    private suspend fun installEntities(
        config: ConfigEntity,
        entities: List<AppEntity>,
        extra: InstallExtraEntity
    ) = AuthorizerInstallerRepoImpl().doWork(
        config,
        entities,
        extra
    )

    private suspend fun finish() {
        worker.impl.progress.emit(ProgressEntity.Finish)
    }
}