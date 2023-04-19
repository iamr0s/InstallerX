package com.rosan.installer.ui.page.installer.dialog

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.accompanist.flowlayout.FlowRow
import com.rosan.installer.R
import com.rosan.installer.data.app.model.entity.AppEntity
import com.rosan.installer.data.app.util.InstalledAppInfo
import com.rosan.installer.data.app.util.sortedBest
import com.rosan.installer.data.common.util.errorInfo
import com.rosan.installer.data.installer.model.entity.error.ResolveError
import com.rosan.installer.data.installer.repo.InstallerRepo
import com.rosan.installer.ui.widget.dialog.PositionDialog
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DialogPage(
    installer: InstallerRepo, viewModel: DialogViewModel = getViewModel {
        parametersOf(installer)
    }
) {
    LaunchedEffect(installer.id) {
        viewModel.dispatch(DialogViewAction.CollectRepo(installer))
    }
    val params = generateParams(installer, viewModel)

    fun innerWidget(widget: @Composable (() -> Unit)?): @Composable (() -> Unit)? =
        if (widget == null) null else {
            {
                AnimatedContent(
                    targetState = widget
                ) {
                    it.invoke()
                }
            }
        }
    PositionDialog(
        onDismissRequest = {
            viewModel.dispatch(DialogViewAction.Background)
        },
        modifier = Modifier.animateContentSize(),
        leftIcon = innerWidget(params.leftIcon),
        centerIcon = innerWidget(params.centerIcon),
        rightIcon = innerWidget(params.rightIcon),
        leftTitle = innerWidget(params.leftTitle),
        centerTitle = innerWidget(params.centerTitle),
        rightTitle = innerWidget(params.rightTitle),
        leftText = innerWidget(params.leftText),
        centerText = innerWidget(params.centerText),
        rightText = innerWidget(params.rightText),
        leftButton = innerWidget(params.leftButton),
        centerButton = innerWidget(params.centerButton),
        rightButton = innerWidget(params.rightButton),
    )
}

@Composable
fun generateParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): AlertDialogParams {
    return when (viewModel.state) {
        is DialogViewState.Ready -> readyParams(installer, viewModel)
        is DialogViewState.Resolving -> resolvingParams(installer, viewModel)
        is DialogViewState.ResolveFailed -> resolveFailedParams(installer, viewModel)
        is DialogViewState.ResolveSuccess -> resolveSuccessParams(installer, viewModel)
        is DialogViewState.Analysing -> analysingParams(installer, viewModel)
        is DialogViewState.AnalyseFailed -> analyseFailedParams(installer, viewModel)
        is DialogViewState.InstallChoice -> installChoiceParams(installer, viewModel)
        is DialogViewState.InstallPrepare -> installPrepareParams(installer, viewModel)
        is DialogViewState.Installing -> installingParams(installer, viewModel)
        is DialogViewState.InstallFailed -> installFailedParams(installer, viewModel)
        is DialogViewState.InstallSuccess -> installSuccessParams(installer, viewModel)
        else -> readyParams(installer, viewModel)
    }
}

private val pausingIcon: @Composable (() -> Unit) = {
    Icon(
        imageVector = Icons.TwoTone.HourglassDisabled, contentDescription = "pausing"
    )
}

private val workingIcon: @Composable (() -> Unit) = {
    Icon(
        imageVector = Icons.TwoTone.HourglassEmpty, contentDescription = "working"
    )
}

private val defaultAppDrawable: @Composable (() -> Drawable?) = {
    ContextCompat.getDrawable(LocalContext.current, android.R.drawable.sym_def_app_icon)
}

private val defaultIcon: @Composable ((Drawable?) -> Unit) = {
    Image(
        modifier = Modifier.size(64.dp),
        painter = rememberDrawablePainter(drawable = it ?: defaultAppDrawable()),
        contentDescription = "working"
    )
}

@Composable
fun readyParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): AlertDialogParams {
    return AlertDialogParams(centerIcon = workingIcon,
        centerTitle = { Text(text = stringResource(R.string.installer_ready)) },
        rightButton = {
            TextButton(onClick = { viewModel.dispatch(DialogViewAction.Close) }) {
                Text(text = stringResource(R.string.cancel))
            }
        })
}

@Composable
fun resolvingParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): AlertDialogParams {
    return AlertDialogParams(centerIcon = workingIcon,
        centerTitle = { Text(text = stringResource(R.string.installer_resolving)) },
        rightButton = {
            TextButton(onClick = { viewModel.dispatch(DialogViewAction.Close) }) {
                Text(text = stringResource(R.string.cancel))
            }
        })
}

@Composable
fun resolveFailedParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): AlertDialogParams {
    return AlertDialogParams(centerIcon = pausingIcon,
        centerTitle = { Text(text = stringResource(R.string.installer_resolve_failed)) },
        leftText = {
            errorText(installer, viewModel)
        },
        rightButton = {
            TextButton(onClick = { viewModel.dispatch(DialogViewAction.Close) }) {
                Text(text = stringResource(R.string.cancel))
            }
        })
}

@Composable
fun resolveSuccessParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): AlertDialogParams {
    return AlertDialogParams(centerIcon = workingIcon,
        centerTitle = { Text(text = stringResource(R.string.installer_resolve_success)) },
        leftText = {
            Text(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                text = installer.data.joinToString(
                    separator = ", ", prefix = "[", postfix = "]"
                )
            )
        },
        rightButton = {
            TextButton(onClick = { viewModel.dispatch(DialogViewAction.Close) }) {
                Text(text = stringResource(R.string.cancel))
            }
        })
}

@Composable
fun analysingParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): AlertDialogParams {
    return AlertDialogParams(centerIcon = workingIcon,
        centerTitle = { Text(text = stringResource(R.string.installer_analysing)) },
        rightButton = {
            TextButton(onClick = { viewModel.dispatch(DialogViewAction.Close) }) {
                Text(text = stringResource(R.string.cancel))
            }
        })
}

@Composable
fun analyseFailedParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): AlertDialogParams {
    return AlertDialogParams(centerIcon = pausingIcon,
        centerTitle = { Text(text = stringResource(R.string.installer_analyse_failed)) },
        leftText = {
            errorText(installer, viewModel)
        },
        rightButton = {
            Row {
                TextButton(onClick = { viewModel.dispatch(DialogViewAction.Close) }) {
                    Text(text = stringResource(R.string.cancel))
                }
                TextButton(onClick = { viewModel.dispatch(DialogViewAction.Analyse) }) {
                    Text(text = stringResource(R.string.retry))
                }
            }
        })
}

@Composable
fun installChoiceParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): AlertDialogParams {
    val entities = installer.entities.toMutableStateList()
    return AlertDialogParams(centerIcon = workingIcon,
        centerTitle = { Text(text = stringResource(R.string.installer_select_install)) },
        leftText = {
            LazyColumn {
                itemsIndexed(entities) { index, item ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            entities[index] = item.copy(selected = !item.selected)
                        }
                        .padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Image(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(32.dp),
                            painter = rememberDrawablePainter(
                                drawable = if (item.app is AppEntity.BaseEntity) item.app.icon
                                    ?: defaultAppDrawable()
                                else null
                            ),
                            contentDescription = "label"
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                        ) {
                            when (val app = item.app) {
                                is AppEntity.BaseEntity -> {
                                    app.label?.let {
                                        Text(
                                            text = it, style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Text(
                                        text = "${app.versionName} (${app.versionCode})",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                is AppEntity.SplitEntity -> {
                                    Text(
                                        text = app.splitName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                is AppEntity.DexMetadataEntity -> {
                                    Text(
                                        text = app.dmName,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = app.packageName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        Checkbox(modifier = Modifier.align(Alignment.CenterVertically),
                            checked = item.selected,
                            onCheckedChange = {
                                entities[index] = item.copy(selected = it)
                            })
                    }
                }
            }
        },
        rightButton = {
            Row {
                TextButton(onClick = { viewModel.dispatch(DialogViewAction.Close) }) {
                    Text(text = stringResource(R.string.cancel))
                }
                TextButton(onClick = {
                    installer.entities = entities
                    viewModel.dispatch(DialogViewAction.InstallPrepare)
                }) {
                    Text(text = stringResource(R.string.next))
                }
            }
        })
}

@Composable
fun installParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): AlertDialogParams {
    val entity = installer.entities.filter { it.selected }.map { it.app }.sortedBest().first()
    val installed = InstalledAppInfo.buildByPackageName(entity.packageName)
    return AlertDialogParams(centerIcon = {
        defaultIcon(
            when (entity) {
                is AppEntity.BaseEntity -> entity.icon
                is AppEntity.SplitEntity -> installed?.icon
                is AppEntity.DexMetadataEntity -> installed?.icon
            }
        )
    }, centerTitle = {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    text = when (entity) {
                        is AppEntity.BaseEntity -> entity.label
                        is AppEntity.SplitEntity -> installed?.label
                        is AppEntity.DexMetadataEntity -> installed?.label
                    } ?: entity.packageName
                )
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    imageVector = Icons.TwoTone.AutoFixHigh,
                    contentDescription = null
                )
            }
            if (installed == null && entity is AppEntity.BaseEntity) Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                text = stringResource(
                    R.string.installer_version,
                    entity.versionName,
                    entity.versionCode
                ),
                style = MaterialTheme.typography.bodyMedium,
                softWrap = false
            ) else if (installed != null && entity is AppEntity.BaseEntity) Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                text = stringResource(
                    R.string.installer_version2,
                    installed.versionName,
                    installed.versionCode,
                    entity.versionName,
                    entity.versionCode
                ),
                style = MaterialTheme.typography.bodyMedium,
                softWrap = false
            )
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                text = stringResource(R.string.installer_package_name, entity.packageName),
                style = MaterialTheme.typography.bodyMedium,
                softWrap = false
            )
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun installPrepareParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): AlertDialogParams {
    val entities = installer.entities
    if (entities.count { it.selected } == 0) return AlertDialogParams(centerIcon = pausingIcon,
        centerTitle = { Text(text = stringResource(R.string.installer_prepare_install)) },
        leftText = { Text(text = stringResource(R.string.installer_prepare_install_empty)) },
        rightButton = {
            Row {
                TextButton(onClick = { viewModel.dispatch(DialogViewAction.Close) }) {
                    Text(text = stringResource(R.string.cancel))
                }
                TextButton(onClick = { viewModel.dispatch(DialogViewAction.InstallChoice) }) {
                    Text(text = stringResource(R.string.previous))
                }
            }
        })
    if (entities.filter { it.selected }
            .groupBy { it.app.packageName }.size > 1) return AlertDialogParams(centerIcon = pausingIcon,
        centerTitle = { Text(text = stringResource(R.string.installer_prepare_install)) },
        leftText = { Text(text = stringResource(R.string.installer_prepare_install_too_many)) },
        rightButton = {
            Row {
                TextButton(onClick = { viewModel.dispatch(DialogViewAction.Close) }) {
                    Text(text = stringResource(R.string.cancel))
                }
                TextButton(onClick = { viewModel.dispatch(DialogViewAction.InstallChoice) }) {
                    Text(text = stringResource(R.string.previous))
                }
            }
        })
    return installParams(installer, viewModel).copy(leftText = {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(text = stringResource(R.string.installer_prepare_install_dsp))
            }
            item {
                FlowRow(
                    mainAxisSpacing = 8.dp
                ) {
                    var forAllUser by remember {
                        mutableStateOf(installer.config.forAllUser)
                    }
                    var allowTestOnly by remember {
                        mutableStateOf(installer.config.allowTestOnly)
                    }
                    var allowDowngrade by remember {
                        mutableStateOf(installer.config.allowDowngrade)
                    }
                    var autoDelete by remember {
                        mutableStateOf(installer.config.autoDelete)
                    }
                    installer.config.forAllUser = forAllUser
                    installer.config.allowTestOnly = allowTestOnly
                    installer.config.allowDowngrade = allowDowngrade
                    installer.config.autoDelete = autoDelete

                    @Composable
                    fun Chip(
                        selected: Boolean,
                        onClick: () -> Unit,
                        label: String,
                        icon: ImageVector? = null,
                    ) {
                        FilterChip(selected = selected,
                            onClick = onClick,
                            leadingIcon = if (icon != null) {
                                {
                                    Icon(
                                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                                        imageVector = icon,
                                        contentDescription = "icon"
                                    )
                                }
                            } else null,
                            label = { Text(text = label) })
                    }

                    Chip(
                        selected = forAllUser,
                        onClick = { forAllUser = !forAllUser },
                        label = stringResource(
                            id = R.string.config_for_all_user
                        ),
                        icon = Icons.TwoTone.People
                    )
                    Chip(
                        selected = allowTestOnly,
                        onClick = { allowTestOnly = !allowTestOnly },
                        label = stringResource(
                            id = R.string.config_allow_test_only
                        ),
                        icon = Icons.TwoTone.BugReport
                    )
                    Chip(
                        selected = allowDowngrade,
                        onClick = { allowDowngrade = !allowDowngrade },
                        label = stringResource(
                            id = R.string.config_allow_downgrade
                        ),
                        icon = Icons.TwoTone.TrendingDown
                    )
                    Chip(
                        selected = autoDelete,
                        onClick = { autoDelete = !autoDelete },
                        label = stringResource(
                            id = R.string.config_auto_delete
                        ),
                        icon = Icons.TwoTone.Delete
                    )
                }
            }
        }
    }, leftButton = {
        TextButton(onClick = { viewModel.dispatch(DialogViewAction.InstallChoice) }) {
            Text(text = stringResource(R.string.previous))
        }
    }, rightButton = {
        Row {
            TextButton(onClick = { viewModel.dispatch(DialogViewAction.Close) }) {
                Text(text = stringResource(R.string.cancel))
            }
            TextButton(onClick = { viewModel.dispatch(DialogViewAction.Install) }) {
                Text(text = stringResource(R.string.install))
            }
        }
    })
}

@Composable
fun installingParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): AlertDialogParams {
    return installParams(
        installer,
        viewModel
    ).copy(centerText = {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    },
        rightButton = {
            TextButton(onClick = { viewModel.dispatch(DialogViewAction.Close) }) {
                Text(text = stringResource(R.string.cancel))
            }
        })
}

@Composable
fun installFailedParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): AlertDialogParams {
    return installParams(installer, viewModel).copy(leftText = {
        errorText(installer, viewModel)
    }, leftButton = {
        TextButton(onClick = { viewModel.dispatch(DialogViewAction.InstallChoice) }) {
            Text(text = stringResource(R.string.previous))
        }
    }, rightButton = {
        Row {
            TextButton(onClick = { viewModel.dispatch(DialogViewAction.Close) }) {
                Text(text = stringResource(R.string.cancel))
            }
            TextButton(onClick = { viewModel.dispatch(DialogViewAction.Install) }) {
                Text(text = stringResource(R.string.retry))
            }
        }
    })
}

@Composable
fun installSuccessParams(
    installer: InstallerRepo, viewModel: DialogViewModel
): AlertDialogParams {
    return installParams(
        installer,
        viewModel
    ).copy(leftText = { Text(text = stringResource(R.string.installer_install_success)) },
        leftButton = {
            TextButton(onClick = { viewModel.dispatch(DialogViewAction.InstallChoice) }) {
                Text(text = stringResource(R.string.previous))
            }
        },
        rightButton = {
            Row {
                TextButton(onClick = { viewModel.dispatch(DialogViewAction.Close) }) {
                    Text(text = stringResource(R.string.finish))
                }
                val context = LocalContext.current
                val packageName =
                    installer.entities.filter { it.selected }.map { it.app }.sortedBest()
                        .first().packageName
                val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (intent != null) TextButton(onClick = {
                    context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    viewModel.dispatch(DialogViewAction.Close)
                }) {
                    Text(text = stringResource(R.string.open))
                }
            }
        })
}

private val errorText: @Composable ((InstallerRepo, DialogViewModel) -> Unit) =
    { installer, viewModel ->
        val text = when (val error = installer.error) {
            is ResolveError -> {
                LocalContext.current.getString(
                    R.string.installer_resolve_error,
                    "${error.action}",
                    error.uris.joinToString(separator = ", ", prefix = "[", postfix = "]")
                )
            }
            else -> error.errorInfo()
        }
        SelectionContainer {
            Text(
                modifier = Modifier.verticalScroll(rememberScrollState()), text = text
            )
        }
    }
