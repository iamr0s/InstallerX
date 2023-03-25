package com.rosan.installer.data.installer.model.impl.installer

import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.work.ForegroundInfo
import com.rosan.installer.R
import com.rosan.installer.data.app.util.getInfo
import com.rosan.installer.data.installer.model.entity.ProgressEntity
import com.rosan.installer.data.installer.model.impl.InstallerRepoImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ForegroundInfoHandler(
    worker: InstallerRepoImpl.MyWorker
) : Handler(worker), KoinComponent {
    companion object {
        private const val InstallerChannel = "installer_channel"

        private const val InstallerBackgroundChannel = "installer_background_channel"

        private const val WorkingIcon = R.drawable.round_hourglass_empty_black_24

        private const val PausingIcon = R.drawable.round_hourglass_disabled_black_24
    }

    private var job: Job? = null

    private val context by inject<Context>()

    private val notificationId = worker.impl.id.hashCode() and Int.MAX_VALUE

    @RequiresApi(Build.VERSION_CODES.O)
    private val notificationChannel: NotificationChannelCompat = NotificationChannelCompat
        .Builder(InstallerChannel, NotificationManagerCompat.IMPORTANCE_HIGH)
        .setName(context.getString(R.string.installer_channel_name))
        .build()

    @RequiresApi(Build.VERSION_CODES.O)
    private val notificationBackgroundChannel: NotificationChannelCompat = NotificationChannelCompat
        .Builder(InstallerBackgroundChannel, NotificationManagerCompat.IMPORTANCE_MIN)
        .setName(context.getString(R.string.installer_background_channel_name))
        .build()

    override suspend fun onStart() {
        job = worker.scope.launch {
            setForeground()
            var enabled = false
            var notification: Notification? = null
            launch {
                worker.impl.progress.collect {
                    notification = when (it) {
                        is ProgressEntity.Ready -> onReady()
                        is ProgressEntity.Resolving -> onResolving()
                        is ProgressEntity.ResolvedFailed -> onResolvedFailed()
                        is ProgressEntity.ResolveSuccess -> onResolveSuccess()
                        is ProgressEntity.Analysing -> onAnalysing()
                        is ProgressEntity.AnalysedFailed -> onAnalysedFailed()
                        is ProgressEntity.AnalysedSuccess -> onAnalysedSuccess()
                        is ProgressEntity.Installing -> onInstalling()
                        is ProgressEntity.InstallFailed -> onInstallFailed()
                        is ProgressEntity.InstallSuccess -> onInstallSuccess()
                        is ProgressEntity.Finish -> null
                        else -> onReady()
                    }
                    setNotification(notification)
                    setNotification(if (enabled) notification else null)
                }
            }
            launch {
                worker.impl.background.collect {
                    enabled = it
                    setNotification(if (enabled) notification else null)
                }
            }
        }
    }

    override suspend fun onFinish() {
        setNotification(null)
        job?.cancel()
    }

    private fun getString(@StringRes resId: Int): String = context.getString(resId)

    private fun setNotification(notification: Notification? = null) {
        val manager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.createNotificationChannel(
            notificationChannel
        )
        if (notification == null) {
            manager.cancel(notificationId)
            return
        }
        manager.notify(notificationId, notification)
    }

    private fun setForeground() {
        val manager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.createNotificationChannel(
            notificationBackgroundChannel
        )
        val notification = NotificationCompat.Builder(context, InstallerBackgroundChannel)
            .setSmallIcon(WorkingIcon)
            .setContentTitle(getString(R.string.installer_running))
            .build()
        worker.setForegroundAsync(
            ForegroundInfo(1, notification)
        )
    }

    private val openIntent =
        BroadcastHandler.openPendingIntent(context, worker)

    private val analyseIntent =
        BroadcastHandler.analysePendingIntent(context, worker)

    private val installIntent =
        BroadcastHandler.installPendingIntent(context, worker)

    private val finishIntent =
        BroadcastHandler.finishPendingIntent(context, worker)

    private fun onReady(): Notification {
        return NotificationCompat.Builder(context, InstallerChannel)
            .setContentIntent(openIntent)
            .setSmallIcon(WorkingIcon)
            .setContentTitle(getString(R.string.installer_ready))
            .addAction(0, getString(R.string.cancel), finishIntent)
            .setDeleteIntent(finishIntent)
            .build()
    }

    private fun onResolving(): Notification {
        return NotificationCompat.Builder(context, InstallerChannel)
            .setContentIntent(openIntent)
            .setSmallIcon(WorkingIcon)
            .setContentTitle(getString(R.string.installer_resolving))
            .addAction(0, getString(R.string.cancel), finishIntent)
            .setDeleteIntent(finishIntent)
            .build()
    }

    private fun onResolvedFailed(): Notification {
        return NotificationCompat.Builder(context, InstallerChannel)
            .setContentIntent(openIntent)
            .setSmallIcon(PausingIcon)
            .setContentTitle(getString(R.string.installer_resolve_failed))
            .addAction(0, getString(R.string.cancel), finishIntent)
            .setDeleteIntent(finishIntent)
            .build()
    }

    private fun onResolveSuccess(): Notification {
        return NotificationCompat.Builder(context, InstallerChannel)
            .setContentIntent(openIntent)
            .setSmallIcon(WorkingIcon)
            .setContentTitle(getString(R.string.installer_resolve_success))
            .addAction(0, getString(R.string.cancel), finishIntent)
            .setDeleteIntent(finishIntent)
            .build()
    }

    private fun onAnalysing(): Notification {
        return NotificationCompat.Builder(context, InstallerChannel)
            .setContentIntent(openIntent)
            .setSmallIcon(WorkingIcon)
            .setContentTitle(getString(R.string.installer_analysing))
            .addAction(0, getString(R.string.cancel), finishIntent)
            .setDeleteIntent(finishIntent)
            .build()
    }

    private fun onAnalysedFailed(): Notification {
        return NotificationCompat.Builder(context, InstallerChannel)
            .setContentIntent(openIntent)
            .setSmallIcon(PausingIcon)
            .setContentTitle(getString(R.string.installer_analyse_failed))
            .addAction(0, getString(R.string.cancel), finishIntent)
            .addAction(0, getString(R.string.retry), analyseIntent)
            .setDeleteIntent(finishIntent)
            .build()
    }

    private fun onAnalysedSuccess(): Notification {
        return if (worker.impl.entities.count { it.selected } != 1)
            NotificationCompat.Builder(context, InstallerChannel)
                .setContentIntent(openIntent)
                .setSmallIcon(WorkingIcon)
                .setContentTitle(getString(R.string.installer_prepare_install))
                .addAction(0, getString(R.string.cancel), finishIntent)
                .setDeleteIntent(finishIntent)
                .build()
        else {
            val info = worker.impl.entities.filter { it.selected }.map { it.app }.getInfo(context)
            NotificationCompat.Builder(context, InstallerChannel)
                .setContentIntent(openIntent)
                .setSmallIcon(WorkingIcon)
                .setContentTitle(info.title)
                .setContentText(getString(R.string.installer_prepare_install_dsp))
                .setLargeIcon(info.icon?.toBitmapOrNull())
                .addAction(0, getString(R.string.install), installIntent)
                .addAction(0, getString(R.string.cancel), finishIntent)
                .setDeleteIntent(finishIntent)
                .build()
        }
    }

    private fun onInstalling(): Notification {
        val info = worker.impl.entities.filter { it.selected }.map { it.app }.getInfo(context)
        return NotificationCompat.Builder(context, InstallerChannel)
            .setContentIntent(openIntent)
            .setSmallIcon(WorkingIcon)
            .setContentTitle(info.title)
            .setContentText(getString(R.string.installer_installing))
            .setLargeIcon(info.icon?.toBitmapOrNull())
            .addAction(0, getString(R.string.cancel), finishIntent)
            .setDeleteIntent(finishIntent)
            .build()
    }

    private fun onInstallFailed(): Notification {
        val info = worker.impl.entities.filter { it.selected }.map { it.app }.getInfo(context)
        return NotificationCompat.Builder(context, InstallerChannel)
            .setContentIntent(openIntent)
            .setSmallIcon(PausingIcon)
            .setContentTitle(info.title)
            .setContentText(getString(R.string.installer_install_failed))
            .setLargeIcon(info.icon?.toBitmapOrNull())
            .addAction(0, getString(R.string.retry), installIntent)
            .addAction(0, getString(R.string.cancel), finishIntent)
            .setDeleteIntent(finishIntent)
            .build()
    }

    private fun onInstallSuccess(): Notification {
        val info = worker.impl.entities.filter { it.selected }.map { it.app }.getInfo(context)
        return NotificationCompat.Builder(context, InstallerChannel)
            .setContentIntent(openIntent)
            .setSmallIcon(WorkingIcon)
            .setContentTitle(info.title)
            .setContentText(getString(R.string.installer_install_success))
            .setLargeIcon(info.icon?.toBitmapOrNull())
            .addAction(0, getString(R.string.finish), finishIntent)
            .setDeleteIntent(finishIntent)
            .build()
    }
}