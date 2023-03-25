package com.rosan.installer.data.installer.model.impl.installer

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import com.rosan.installer.data.installer.model.impl.InstallerRepoImpl
import com.rosan.installer.ui.activity.InstallerActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BroadcastHandler(worker: InstallerRepoImpl.MyWorker) : Handler(worker), KoinComponent {
    companion object {
        private const val ACTION = "installer.broadcast.action"

        const val KEY_ID = "installer_id"

        private const val KEY_NAME = "name"

        private fun workerIntent(worker: InstallerRepoImpl.MyWorker) = Intent(ACTION)
            .putExtra(KEY_ID, worker.impl.id)

        fun openIntent(worker: InstallerRepoImpl.MyWorker) = workerIntent(worker)
            .putExtra(KEY_NAME, Name.Open.value)

        fun analyseIntent(worker: InstallerRepoImpl.MyWorker) = workerIntent(worker)
            .putExtra(KEY_NAME, Name.Analyse.value)

        fun installIntent(worker: InstallerRepoImpl.MyWorker) = workerIntent(worker)
            .putExtra(KEY_NAME, Name.Install.value)

        fun finishIntent(worker: InstallerRepoImpl.MyWorker) = workerIntent(worker)
            .putExtra(KEY_NAME, Name.Finish.value)

        fun openPendingIntent(
            context: Context,
            worker: InstallerRepoImpl.MyWorker
        ): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                worker.impl.id.hashCode() + Name.Open.ordinal,
                openIntent(worker),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                else PendingIntent.FLAG_UPDATE_CURRENT
            )

        fun analysePendingIntent(
            context: Context,
            worker: InstallerRepoImpl.MyWorker
        ): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                worker.impl.id.hashCode() + Name.Analyse.ordinal,
                analyseIntent(worker),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                else PendingIntent.FLAG_UPDATE_CURRENT
            )

        fun installPendingIntent(
            context: Context,
            worker: InstallerRepoImpl.MyWorker
        ): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                worker.impl.id.hashCode() + Name.Install.ordinal,
                installIntent(worker),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                else PendingIntent.FLAG_UPDATE_CURRENT
            )

        fun finishPendingIntent(
            context: Context,
            worker: InstallerRepoImpl.MyWorker
        ): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                worker.impl.id.hashCode() + Name.Finish.ordinal,
                finishIntent(worker),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                else PendingIntent.FLAG_UPDATE_CURRENT
            )
    }

    private val context by inject<Context>()

    private val receiver = Receiver(worker)

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

    private class Receiver(private val worker: InstallerRepoImpl.MyWorker) : BroadcastReceiver(),
        KoinComponent {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            if (intent.action != ACTION) return
            if (intent.getStringExtra(KEY_ID) != worker.impl.id) return
            val name = intent.getStringExtra(KEY_NAME).let { name ->
                name ?: return@let null
                Name.values().find { it.value == name }
            } ?: return
            doWork(name)
        }

        private fun doWork(name: Name) {
            when (name) {
                Name.Open -> {
                    val context by inject<Context>()
                    context.startActivity(
                        Intent(context, InstallerActivity::class.java)
                            .putExtra(InstallerActivity.KEY_ID, worker.impl.id)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                Name.Analyse -> worker.impl.analyse()
                Name.Install -> worker.impl.install()
                Name.Finish -> worker.impl.close()
            }
        }
    }

    enum class Name(val value: String) {
        Open("open"),
        Analyse("analyse"),
        Install("install"),
        Finish("finish");
    }
}