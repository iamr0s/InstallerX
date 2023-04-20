package com.rosan.installer.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rosan.installer.data.installer.model.entity.ProgressEntity
import com.rosan.installer.data.installer.repo.InstallerRepo
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import com.rosan.installer.data.settings.util.ConfigUtil
import com.rosan.installer.ui.page.installer.InstallerPage
import com.rosan.installer.ui.theme.InstallerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class InstallerActivity : ComponentActivity(), KoinComponent {
    companion object {
        const val KEY_ID = "installer_id"
    }

    private var installer by mutableStateOf<InstallerRepo?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restoreInstaller(savedInstanceState)
        showContent()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_ID, installer?.id)
        super.onSaveInstanceState(outState)
    }

    override fun onNewIntent(intent: Intent?) {
        this.intent = intent
        super.onNewIntent(intent)
        restoreInstaller()
    }

    private var progressCollectJob: Job? = null

    private fun restoreInstaller(savedInstanceState: Bundle? = null) {
        progressCollectJob?.cancel()
        val installerId = if (savedInstanceState == null)
            intent?.getStringExtra(KEY_ID)
        else savedInstanceState.getString(KEY_ID)
        installer?.background(true)
        val installer: InstallerRepo = get {
            parametersOf(installerId)
        }
        installer.background(false)
        this.installer = installer
        val scope = CoroutineScope(Dispatchers.IO)
        progressCollectJob = scope.launch {
            installer.progress.collect { progress ->
                when (progress) {
                    is ProgressEntity.Ready -> {
                        installer.config = currentConfig()
                        val installMode = installer.config.installMode
                        if (
                            installMode == ConfigEntity.InstallMode.Notification
                            || installMode == ConfigEntity.InstallMode.AutoNotification
                        ) {
                            installer.background(true)
                        }
                        installer.resolve(this@InstallerActivity)
                    }

                    is ProgressEntity.Finish -> {
                        val activity = this@InstallerActivity
                        if (!activity.isFinishing) activity.finish()
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun currentConfig(): ConfigEntity {
        val activity = this
        val packageName = activity.callingPackage
            ?: (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) activity.referrer?.host else null)
        var config = ConfigUtil.getByPackageName(packageName)
        if (config.installer == null) config = config.copy(
            installer = packageName
        )
        return config
    }

    private fun showContent() {
        setContent {
            var hideDialog by remember {
                mutableStateOf(true)
            }

            val installer = installer ?: return@setContent
            LaunchedEffect(installer.id) {
                launch {
                    installer.background.collect {
                        if (it) this@InstallerActivity.finish()
                    }
                }
                launch {
                    installer.progress.collect {
                        hideDialog = it is ProgressEntity.Ready ||
                                it is ProgressEntity.Resolving ||
                                it is ProgressEntity.Finish
                    }
                }
            }
            if (hideDialog) return@setContent
            InstallerTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                ) {
                    InstallerPage(installer)
                }
            }
        }
    }
}