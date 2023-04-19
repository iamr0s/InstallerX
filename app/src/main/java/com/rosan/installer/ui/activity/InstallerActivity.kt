package com.rosan.installer.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.rosan.installer.data.installer.model.entity.ProgressEntity
import com.rosan.installer.data.installer.repo.InstallerRepo
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

    private fun showContent() {
        setContent {
            val installer = installer ?: return@setContent
            LaunchedEffect(installer.id) {
                launch {
                    installer.background.collect {
                        if (it) this@InstallerActivity.finish()
                    }
                }
            }
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