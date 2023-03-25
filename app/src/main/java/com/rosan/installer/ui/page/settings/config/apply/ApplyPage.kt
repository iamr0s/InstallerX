package com.rosan.installer.ui.page.settings.config.apply

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.rosan.installer.R
import com.rosan.installer.ui.common.ViewContent
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ApplyPage(
    navController: NavController,
    id: Long,
    viewModel: ApplyViewModel = getViewModel {
        parametersOf(id)
    }
) {
    LaunchedEffect(true) {
        viewModel.dispatch(ApplyViewAction.Init)
    }

    val snackBarHostState = remember {
        SnackbarHostState()
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app))
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.TwoTone.ArrowBack,
                            contentDescription = stringResource(
                                id = R.string.back
                            )
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) {
        Box(modifier = Modifier.padding(it)) {
            when {
                viewModel.state.apps.progress is ViewContent.Progress.Loading
                        && viewModel.state.apps.data.isEmpty() -> {
                    LottieWidget(
                        contentPadding = it,
                        spec = LottieCompositionSpec.RawRes(R.raw.loading),
                        text = stringResource(id = R.string.loading)
                    )
                }
                else -> {
                    val refreshing = viewModel.state.apps.progress is ViewContent.Progress.Loading
                    val pullRefreshState = rememberPullRefreshState(
                        refreshing = refreshing,
                        onRefresh = { viewModel.dispatch(ApplyViewAction.LoadApps) }
                    )
                    Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                        ItemsWidget(
                            modifier = Modifier
                                .fillMaxSize(),
                            viewModel = viewModel,
                        )
                        PullRefreshIndicator(
                            modifier = Modifier.align(Alignment.TopCenter),
                            refreshing = refreshing,
                            state = pullRefreshState
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LottieWidget(
    contentPadding: PaddingValues,
    spec: LottieCompositionSpec,
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val composition by rememberLottieComposition(spec)
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever,
            )
            LottieAnimation(
                modifier = Modifier
                    .size(200.dp),
                composition = composition,
                progress = { progress }
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun ItemsWidget(
    modifier: Modifier,
    viewModel: ApplyViewModel
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            ItemWidget(
                viewModel = viewModel,
                app = null
            )
        }
        items(viewModel.state.apps.data) {
            ItemWidget(
                viewModel = viewModel,
                app = it
            )
        }
    }
}

@Composable
fun ItemWidget(
    viewModel: ApplyViewModel,
    app: ApplyViewAppInfo?,
) {
    val applied =
        viewModel.state.appEntities.data.find { it.packageName == app?.packageName } != null
    Box(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.dispatch(
                        ApplyViewAction.ApplyPackageName(
                            app?.packageName,
                            !applied
                        )
                    )
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AsyncImage(
                model = app?.icon,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterVertically),
                contentDescription = app?.label
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                if (app != null) {
                    Text(
                        text = app.label ?: app.packageName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${app.versionName} (${app.versionCode})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.global),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Checkbox(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                checked = applied,
                onCheckedChange = {
                    viewModel.dispatch(
                        ApplyViewAction.ApplyPackageName(
                            app?.packageName,
                            it
                        )
                    )
                }
            )
        }
    }
}
