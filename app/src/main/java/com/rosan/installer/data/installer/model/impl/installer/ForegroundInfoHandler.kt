package com.rosan.installer.data.installer.model.impl.installer

import android.app.Notification
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmapOrNull
import com.rosan.installer.R
import com.rosan.installer.data.app.util.getInfo
import com.rosan.installer.data.installer.model.entity.ProgressEntity
import com.rosan.installer.data.installer.repo.InstallerRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ForegroundInfoHandler(scope: CoroutineScope, installer: InstallerRepo) :
    Handler(scope, installer), KoinComponent {
    enum class Channel(val value: String) {
        InstallerChannel("installer_channel"), InstallerProgressChannel("installer_progress_channel")
    }

    enum class Icon(@DrawableRes val resId: Int) {
        Working(R.drawable.round_hourglass_empty_black_24), Pausing(R.drawable.round_hourglass_disabled_black_24)
    }

    private var job: Job? = null

    private val context by inject<Context>()

    private val notificationManager = NotificationManagerCompat.from(context)

    private val notificationId = installer.id.hashCode() and Int.MAX_VALUE

    private val notificationChannels = mapOf(
        Channel.InstallerChannel to NotificationChannelCompat.Builder(
            Channel.InstallerChannel.value, NotificationManagerCompat.IMPORTANCE_MAX
        ).setName(getString(R.string.installer_channel_name)).build(),

        Channel.InstallerProgressChannel to NotificationChannelCompat.Builder(
            Channel.InstallerProgressChannel.value, NotificationManagerCompat.IMPORTANCE_MIN
        ).setName(getString(R.string.installer_progress_channel_name)).build()
    )

    private val workingProgresses = listOf(
        ProgressEntity.Ready,
        ProgressEntity.Resolving,
        ProgressEntity.ResolveSuccess,
        ProgressEntity.Analysing,
        ProgressEntity.AnalysedSuccess,
        ProgressEntity.Installing,
        ProgressEntity.InstallSuccess
    )

    private val importanceProgresses = listOf(
        ProgressEntity.ResolvedFailed,
        ProgressEntity.AnalysedFailed,
        ProgressEntity.AnalysedSuccess,
        ProgressEntity.InstallFailed,
        ProgressEntity.InstallSuccess
    )

    private val installProgresses = mapOf(
        ProgressEntity.Resolving to 0,
        ProgressEntity.Analysing to 40,
        ProgressEntity.Installing to 80
    )

    private fun newNotificationBuilder(
        progress: ProgressEntity, background: Boolean
    ): NotificationCompat.Builder {
        val isWorking = workingProgresses.contains(progress)
        val isImportance = importanceProgresses.contains(progress)

        val channel =
            notificationChannels[if (isImportance && background) Channel.InstallerChannel else Channel.InstallerProgressChannel]!!

        notificationManager.createNotificationChannel(channel)

        val icon = (if (isWorking) Icon.Working else Icon.Pausing).resId

        var builder = NotificationCompat.Builder(context, channel.id).setSmallIcon(icon)
            .setContentIntent(openIntent)
            .setDeleteIntent(finishIntent)

        installProgresses[progress]?.let {
            builder = builder.setProgress(100, it, false)
        }

        return builder
    }

    private fun newNotification(
        progress: ProgressEntity, background: Boolean
    ): Notification? {
        val builder = newNotificationBuilder(progress, background)
        return when (progress) {
            is ProgressEntity.Ready -> onReady(builder)
            is ProgressEntity.Resolving -> onResolving(builder)
            is ProgressEntity.ResolvedFailed -> onResolvedFailed(builder)
            is ProgressEntity.ResolveSuccess -> onResolveSuccess(builder)
            is ProgressEntity.Analysing -> onAnalysing(builder)
            is ProgressEntity.AnalysedFailed -> onAnalysedFailed(builder)
            is ProgressEntity.AnalysedSuccess -> onAnalysedSuccess(builder)
            is ProgressEntity.Installing -> onInstalling(builder)
            is ProgressEntity.InstallFailed -> onInstallFailed(builder)
            is ProgressEntity.InstallSuccess -> onInstallSuccess(builder)
            is ProgressEntity.Finish -> null
            else -> onReady(builder)
        }
    }

    override suspend fun onStart() {
        job = scope.launch {
            var progress: ProgressEntity = ProgressEntity.Ready
            var background = false
            fun refresh() {
                setNotification(newNotification(progress, background))
            }
            launch {
                installer.progress.collect {
                    progress = it
                    refresh()
                }
            }
            launch {
                installer.background.collect {
                    background = it
                    refresh()
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
        if (notification == null) {
            notificationManager.cancel(notificationId)
            return
        }
        notificationManager.notify(notificationId, notification)
    }

    private val openIntent = BroadcastHandler.openIntent(context, installer)

    private val analyseIntent =
        BroadcastHandler.namedIntent(context, installer, BroadcastHandler.Name.Analyse)

    private val installIntent =
        BroadcastHandler.namedIntent(context, installer, BroadcastHandler.Name.Install)

    private val finishIntent =
        BroadcastHandler.namedIntent(context, installer, BroadcastHandler.Name.Finish)

    private fun onReady(builder: NotificationCompat.Builder) =
        builder.setContentTitle(getString(R.string.installer_ready))
            .addAction(0, getString(R.string.cancel), finishIntent).build()

    private fun onResolving(builder: NotificationCompat.Builder) =
        builder.setContentTitle(getString(R.string.installer_resolving))
            .addAction(0, getString(R.string.cancel), finishIntent).build()

    private fun onResolvedFailed(builder: NotificationCompat.Builder) =
        builder.setContentTitle(getString(R.string.installer_resolve_failed))
            .addAction(0, getString(R.string.cancel), finishIntent).build()

    private fun onResolveSuccess(builder: NotificationCompat.Builder) =
        builder.setContentTitle(getString(R.string.installer_resolve_success))
            .addAction(0, getString(R.string.cancel), finishIntent).build()

    private fun onAnalysing(builder: NotificationCompat.Builder) =
        builder.setContentTitle(getString(R.string.installer_analysing))
            .addAction(0, getString(R.string.cancel), finishIntent).build()

    private fun onAnalysedFailed(builder: NotificationCompat.Builder) =
        builder.setContentTitle(getString(R.string.installer_analyse_failed))
            .addAction(0, getString(R.string.retry), analyseIntent)
            .addAction(0, getString(R.string.cancel), finishIntent).build()

    private fun onAnalysedSuccess(builder: NotificationCompat.Builder): Notification {
        val selected = installer.entities.filter { it.selected }
        return (if (selected.groupBy { it.app.packageName }.size != 1) builder.setContentTitle(
            getString(R.string.installer_prepare_install)
        ).addAction(0, getString(R.string.cancel), finishIntent)
        else {
            val info = selected.map { it.app }.getInfo(context)
            builder.setContentTitle(info.title)
                .setContentText(getString(R.string.installer_prepare_install_dsp))
                .setLargeIcon(info.icon?.toBitmapOrNull())
                .addAction(0, getString(R.string.install), installIntent)
                .addAction(0, getString(R.string.cancel), finishIntent)
        }).build()
    }

    private fun onInstalling(builder: NotificationCompat.Builder): Notification {
        val info = installer.entities.filter { it.selected }.map { it.app }.getInfo(context)
        return builder.setContentTitle(info.title)
            .setContentText(getString(R.string.installer_installing))
            .setLargeIcon(info.icon?.toBitmapOrNull())
            .addAction(0, getString(R.string.cancel), finishIntent).build()
    }

    private fun onInstallFailed(builder: NotificationCompat.Builder): Notification {
        val info = installer.entities.filter { it.selected }.map { it.app }.getInfo(context)
        return builder.setContentTitle(info.title)
            .setContentText(getString(R.string.installer_install_failed))
            .setLargeIcon(info.icon?.toBitmapOrNull())
            .addAction(0, getString(R.string.retry), installIntent)
            .addAction(0, getString(R.string.cancel), finishIntent).build()
    }

    private fun onInstallSuccess(builder: NotificationCompat.Builder): Notification {
        val entities = installer.entities.filter { it.selected }.map { it.app }
        val info = entities.getInfo(context)
        val launchIntent =
            context.packageManager.getLaunchIntentForPackage(entities.first().packageName)
        val launchPendingIntent = launchIntent?.let {
            BroadcastHandler.launchIntent(context, installer, it)
        }

        var newBuilder = builder.setContentTitle(info.title)
            .setContentText(getString(R.string.installer_install_success))
            .setLargeIcon(info.icon?.toBitmapOrNull())
        if (launchIntent != null) newBuilder =
            newBuilder.addAction(0, getString(R.string.open), launchPendingIntent)
        return newBuilder
            .addAction(0, getString(R.string.finish), finishIntent)
            .build()
    }
}