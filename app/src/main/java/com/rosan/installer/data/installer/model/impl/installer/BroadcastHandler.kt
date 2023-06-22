package com.rosan.installer.data.installer.model.impl.installer

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import com.rosan.installer.data.app.util.sortedBest
import com.rosan.installer.data.installer.repo.InstallerRepo
import com.rosan.installer.ui.activity.InstallerActivity
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BroadcastHandler(scope: CoroutineScope, installer: InstallerRepo) :
    Handler(scope, installer), KoinComponent {
    companion object {
        private const val ACTION = "installer.broadcast.action"

        const val KEY_ID = "installer_id"

        private const val KEY_NAME = "name"

        private fun installerIntent(installer: InstallerRepo) = Intent(ACTION)
            .putExtra(KEY_ID, installer.id)

        fun openIntent(installer: InstallerRepo) = installerIntent(installer)
            .putExtra(KEY_NAME, Name.Open.value)

        fun analyseIntent(installer: InstallerRepo) = installerIntent(installer)
            .putExtra(KEY_NAME, Name.Analyse.value)

        fun installIntent(installer: InstallerRepo) = installerIntent(installer)
            .putExtra(KEY_NAME, Name.Install.value)

        fun finishIntent(installer: InstallerRepo) = installerIntent(installer)
            .putExtra(KEY_NAME, Name.Finish.value)

        fun launchIntent(installer: InstallerRepo) = installerIntent(installer)
            .putExtra(KEY_NAME, Name.Launch.value)

        fun openPendingIntent(
            context: Context,
            installer: InstallerRepo
        ): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                installer.id.hashCode() + Name.Open.ordinal,
                openIntent(installer),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                else PendingIntent.FLAG_UPDATE_CURRENT
            )

        fun analysePendingIntent(
            context: Context,
            installer: InstallerRepo
        ): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                installer.id.hashCode() + Name.Analyse.ordinal,
                analyseIntent(installer),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                else PendingIntent.FLAG_UPDATE_CURRENT
            )

        fun installPendingIntent(
            context: Context,
            installer: InstallerRepo
        ): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                installer.id.hashCode() + Name.Install.ordinal,
                installIntent(installer),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                else PendingIntent.FLAG_UPDATE_CURRENT
            )

        fun finishPendingIntent(
            context: Context,
            installer: InstallerRepo
        ): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                installer.id.hashCode() + Name.Finish.ordinal,
                finishIntent(installer),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                else PendingIntent.FLAG_UPDATE_CURRENT
            )

        fun launchPendingIntent(
            context: Context,
            installer: InstallerRepo
        ): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                installer.id.hashCode() + Name.Launch.ordinal,
                launchIntent(installer),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                else PendingIntent.FLAG_UPDATE_CURRENT
            )
    }

    private val context by inject<Context>()

    private val receiver = Receiver(installer)

    override suspend fun onStart() {
        registerReceiver(receiver)
    }

    private fun registerReceiver(receiver: Receiver) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) registerReceiverTiramisu(receiver)
        else context.registerReceiver(receiver, IntentFilter(ACTION))
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun registerReceiverTiramisu(receiver: Receiver) {
        context.registerReceiver(
            receiver,
            IntentFilter(ACTION),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    override suspend fun onFinish() {
        context.unregisterReceiver(receiver)
    }

    private class Receiver(private val installer: InstallerRepo) : BroadcastReceiver(),
        KoinComponent {
        private val context by inject<Context>()

        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            if (intent.action != ACTION) return
            if (intent.getStringExtra(KEY_ID) != installer.id) return
            val name = intent.getStringExtra(KEY_NAME).let { name ->
                name ?: return@let null
                Name.values().find { it.value == name }
            } ?: return
            doWork(name)
        }

        private fun doWork(name: Name) {
            when (name) {
                Name.Open -> {
                    context.startActivity(
                        Intent(context, InstallerActivity::class.java)
                            .putExtra(InstallerActivity.KEY_ID, installer.id)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }

                Name.Analyse -> installer.analyse()
                Name.Install -> installer.install()
                Name.Finish -> installer.close()
                Name.Launch -> {
                    val packageName =
                        installer.entities.filter { it.selected }.map { it.app }.sortedBest()
                            .first().packageName
                    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                    if (intent != null) context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    installer.close()
                }
            }
        }
    }

    enum class Name(val value: String) {
        Open("open"),
        Analyse("analyse"),
        Install("install"),
        Finish("finish"),
        Launch("launch");
    }
}