package com.rosan.installer.data.installer.model.impl

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.work.*
import com.rosan.installer.data.app.model.entity.DataEntity
import com.rosan.installer.data.installer.model.entity.ProgressEntity
import com.rosan.installer.data.installer.model.entity.SelectInstallEntity
import com.rosan.installer.data.installer.model.impl.installer.ActionHandler
import com.rosan.installer.data.installer.model.impl.installer.BroadcastHandler
import com.rosan.installer.data.installer.model.impl.installer.ForegroundInfoHandler
import com.rosan.installer.data.installer.model.impl.installer.ProgressHandler
import com.rosan.installer.data.installer.repo.InstallerRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.*
import kotlin.collections.set

class InstallerRepoImpl private constructor() : InstallerRepo, KoinComponent {
    companion object : KoinComponent {
        private val impls = mutableMapOf<String, InstallerRepoImpl>()

        fun getOrCreate(id: String? = null): InstallerRepo {
            if (id == null) return create()
            return get(id) ?: create()
        }

        fun get(id: String): InstallerRepo? {
            return impls[id]
        }

        fun create(): InstallerRepo {
            val impl = InstallerRepoImpl()
            impls[impl.id] = impl
            val manager: WorkManager = get()
            val data = workDataOf(
                "installer_id" to impl.id
            )
            val request = OneTimeWorkRequestBuilder<MyWorker>()
                .setInputData(data)
                .build()
            manager.beginUniqueWork(impl.id, ExistingWorkPolicy.KEEP, request)
                .enqueue()
            return impl
        }

        fun remove(id: String) {
            impls.remove(id)
        }
    }

    override val id: String = UUID.randomUUID().toString()

    override var error: Throwable = Throwable()

    override var config: ConfigEntity = ConfigEntity.default

    override var data: List<DataEntity> by mutableStateOf(emptyList())

    override var entities: List<SelectInstallEntity> by mutableStateOf(emptyList())

    override val progress: MutableSharedFlow<ProgressEntity> =
        MutableStateFlow(ProgressEntity.Ready)

    val action: MutableSharedFlow<Action> =
        MutableSharedFlow(replay = 1, extraBufferCapacity = 1)

    override val background: MutableSharedFlow<Boolean> =
        MutableStateFlow(false)

    private val isWorking: Boolean = runBlocking {
        val manager: WorkManager = get()
        manager.getWorkInfosForUniqueWork(id).await().forEach {
            if (!it.state.isFinished) return@runBlocking true
        }
        false
    }

    override fun resolve(activity: Activity) {
        action.tryEmit(Action.Resolve(activity))
    }

    override fun analyse() {
        action.tryEmit(Action.Analyse)
    }

    override fun install() {
        action.tryEmit(Action.Install)
    }

    override fun background(value: Boolean) {
        background.tryEmit(value)
    }

    override fun close() {
        if (isWorking) {
            action.tryEmit(Action.Finish)
        } else {
            progress.tryEmit(ProgressEntity.Finish)
        }
        remove(id)
    }

    sealed class Action {
        data class Resolve(val activity: Activity) : Action()

        object Analyse : Action()

        object Install : Action()

        object Finish : Action()
    }

    class MyWorker(context: Context, workerParams: WorkerParameters) :
        CoroutineWorker(context, workerParams), KoinComponent {
        private var _impl: InstallerRepoImpl? = null

        var impl: InstallerRepoImpl
            get() = _impl!!
            set(value) {
                _impl = value
            }

        lateinit var scope: CoroutineScope

        override suspend fun doWork(): Result {
            val exception = kotlin.runCatching {
                doInnerWork()
                _impl?.progress?.tryEmit(ProgressEntity.Finish)
            }.exceptionOrNull()
            exception ?: return Result.success()
            if (exception is CancellationException) return Result.success()
            // 异常结束
            impl.error = exception
            impl.progress.tryEmit(ProgressEntity.Error)
            return Result.failure()
            // 意外退出，重新开启
            // doWork()
        }

        private suspend fun doInnerWork() = withContext(Dispatchers.IO) {
            scope = this
            val installerId =
                inputData.getString("installer_id") ?: return@withContext Result.failure()
            impl = impls[installerId] ?: return@withContext Result.failure()
            val handlers = listOf(
                ActionHandler(this@MyWorker),
                ProgressHandler(this@MyWorker),
                ForegroundInfoHandler(this@MyWorker),
                BroadcastHandler(this@MyWorker)
            )
            handlers.forEach {
                it.onStart()
            }
            var onFinishJob: Job? = null
            onFinishJob = launch {
                impl.progress.collect {
                    if (it is ProgressEntity.Finish) {
                        handlers.forEach {
                            it.onFinish()
                        }
                        onFinishJob?.cancel()
                    }
                }
            }
        }
    }
}