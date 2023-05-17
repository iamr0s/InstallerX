package com.rosan.installer.ui.page.settings.preferred

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.rosan.installer.BuildConfig
import com.rosan.installer.R
import com.rosan.installer.data.app.model.impl.ds.AuthorizerDSRepoImpl
import com.rosan.installer.data.settings.util.ConfigUtil
import com.rosan.installer.ui.activity.InstallerActivity
import com.rosan.installer.ui.widget.setting.BaseWidget
import com.rosan.installer.ui.widget.setting.LabelWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.getKoin
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferredPage(navController: NavController) {
    val snackBarHostState = remember {
        SnackbarHostState()
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.preferred))
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            item { LabelWidget(label = stringResource(id = R.string.basic)) }
            item { LockDefaultInstaller() }
            item { UnlockDefaultInstaller() }
            item { ClearCache() }
            item { LabelWidget(label = stringResource(id = R.string.more)) }
            item { UserTerms() }
            item { PrivacyPolicy() }
        }
    }
}

fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

@Composable
fun LockDefaultInstaller() {
    val scope = rememberCoroutineScope()
    val koin = getKoin()
    var inProgress by remember {
        mutableStateOf(false)
    }
    BaseWidget(
        icon = Icons.TwoTone.Favorite,
        title = stringResource(id = R.string.lock_default_installer),
        onClick = {

            if (inProgress) return@BaseWidget
            inProgress = true
            scope.launch(Dispatchers.IO) {
                val error = kotlin.runCatching {
                    AuthorizerDSRepoImpl().doWork(
                        ConfigUtil.getByPackageName(BuildConfig.APPLICATION_ID),
                        BuildConfig.APPLICATION_ID,
                        InstallerActivity::class.java.name,
                        true
                    )
                }.exceptionOrNull()
                inProgress = false
            }
        }
    ) {}
}

@Composable
fun UnlockDefaultInstaller() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val koin = getKoin()
    var inProgress by remember {
        mutableStateOf(false)
    }
    BaseWidget(
        icon = Icons.TwoTone.FavoriteBorder,
        title = stringResource(id = R.string.unlock_default_installer),
        onClick = {
            if (inProgress) return@BaseWidget
            inProgress = true
            scope.launch(Dispatchers.IO) {
                val error = kotlin.runCatching {
                    AuthorizerDSRepoImpl().doWork(
                        ConfigUtil.getByPackageName(BuildConfig.APPLICATION_ID),
                        BuildConfig.APPLICATION_ID,
                        InstallerActivity::class.java.name,
                        false
                    )
                }.exceptionOrNull()
                inProgress = false
            }
        }
    ) {}
}

@Composable
fun ClearCache() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var inProgress by remember {
        mutableStateOf(false)
    }
    BaseWidget(
        icon = Icons.TwoTone.ClearAll,
        title = stringResource(id = R.string.clear_cache),
        onClick = {
            if (inProgress) return@BaseWidget
            inProgress = true
            scope.launch(Dispatchers.IO) {
                val paths = listOfNotNull(
                    context.externalCacheDir?.absolutePath
                )

                fun clearFile(file: File) {
                    if (!file.exists()) return
                    if (file.isDirectory) file.listFiles()?.forEach {
                        clearFile(it)
                    }
                    file.delete()
                }

                paths.forEach {
                    clearFile(File(it))
                }
                inProgress = false
            }
        }
    ) {}
}

@Composable
fun UserTerms() {
    val context = LocalContext.current
    BaseWidget(
        icon = Icons.TwoTone.Gavel,
        title = stringResource(id = R.string.user_terms),
        onClick = {
            openUrl(context, "https://iamr0s.github.io/InstallerXDocs/terms")
        }
    ) {}
}

@Composable
fun PrivacyPolicy() {
    val context = LocalContext.current
    BaseWidget(
        icon = Icons.TwoTone.PrivacyTip,
        title = stringResource(id = R.string.privacy_policy),
        onClick = {
            openUrl(context, "https://iamr0s.github.io/InstallerXDocs/privacy")
        }
    ) {}
}