package com.rosan.installer.ui.page.installer.dialog.inner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.HourglassDisabled
import androidx.compose.material.icons.twotone.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rosan.installer.R
import com.rosan.installer.data.installer.repo.InstallerRepo
import com.rosan.installer.data.recycle.model.exception.AppProcessNotWorkException
import com.rosan.installer.data.recycle.model.exception.DhizukuNotWorkException
import com.rosan.installer.data.recycle.model.exception.RootNotWorkException
import com.rosan.installer.data.recycle.model.exception.ShizukuNotWorkException
import com.rosan.installer.ui.page.installer.dialog.DialogViewModel

val pausingIcon: @Composable () -> Unit = {
    Icon(
        imageVector = Icons.TwoTone.HourglassDisabled, contentDescription = null
    )
}

val workingIcon: @Composable () -> Unit = {
    Icon(
        imageVector = Icons.TwoTone.HourglassEmpty, contentDescription = null
    )
}

val errorText: ((installer: InstallerRepo, viewModel: DialogViewModel) -> (@Composable () -> Unit)) =
    { installer, viewModel ->
        {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onErrorContainer) {
                LazyColumn(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .fillMaxWidth()
                        .padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        val text = when (val error = installer.error) {
                            is com.rosan.installer.data.app.model.exception.InstallFailedAlreadyExistsException -> stringResource(R.string.exception_install_failed_already_exists)
                            is com.rosan.installer.data.app.model.exception.InstallFailedInvalidAPKException -> stringResource(R.string.exception_install_failed_invalid_apk)
                            is com.rosan.installer.data.app.model.exception.InstallFailedInvalidURIException -> stringResource(R.string.exception_install_failed_invalid_uri)
                            is com.rosan.installer.data.app.model.exception.InstallFailedInsufficientStorageException -> stringResource(R.string.exception_install_failed_insufficient_storage)
                            is com.rosan.installer.data.app.model.exception.InstallFailedDuplicatePackageException -> stringResource(R.string.exception_install_failed_duplicate_package)
                            is com.rosan.installer.data.app.model.exception.InstallFailedNoSharedUserException -> stringResource(R.string.exception_install_failed_no_shared_user)
                            is com.rosan.installer.data.app.model.exception.InstallFailedUpdateIncompatibleException -> stringResource(R.string.exception_install_failed_update_incompatible)
                            is com.rosan.installer.data.app.model.exception.InstallFailedSharedUserIncompatibleException -> stringResource(R.string.exception_install_failed_shared_user_incompatible)
                            is com.rosan.installer.data.app.model.exception.InstallFailedMissingSharedLibraryException -> stringResource(R.string.exception_install_failed_missing_shared_library)
                            is com.rosan.installer.data.app.model.exception.InstallFailedReplaceCouldntDeleteException -> stringResource(R.string.exception_install_failed_replace_couldnt_delete)
                            is com.rosan.installer.data.app.model.exception.InstallFailedDexoptException -> stringResource(R.string.exception_install_failed_dexopt)
                            is com.rosan.installer.data.app.model.exception.InstallFailedOlderSdkException -> stringResource(R.string.exception_install_failed_older_sdk)
                            is com.rosan.installer.data.app.model.exception.InstallFailedConflictingProviderException -> stringResource(R.string.exception_install_failed_conflicting_provider)
                            is com.rosan.installer.data.app.model.exception.InstallFailedNewerSDKException -> stringResource(R.string.exception_install_failed_newer_sdk)
                            is com.rosan.installer.data.app.model.exception.InstallFailedTestOnlyException -> stringResource(R.string.exception_install_failed_test_only)
                            is com.rosan.installer.data.app.model.exception.InstallFailedCpuAbiIncompatibleException -> stringResource(R.string.exception_install_failed_cpu_abi_incompatible)
                            is com.rosan.installer.data.app.model.exception.InstallFailedMissingFeatureException -> stringResource(R.string.exception_install_failed_missing_feature)
                            is com.rosan.installer.data.app.model.exception.InstallFailedContainerErrorException -> stringResource(R.string.exception_install_failed_container_error)
                            is com.rosan.installer.data.app.model.exception.InstallFailedInvalidInstallLocationException -> stringResource(R.string.exception_install_failed_invalid_install_location)
                            is com.rosan.installer.data.app.model.exception.InstallFailedMediaUnavailableException -> stringResource(R.string.exception_install_failed_media_unavailable)
                            is com.rosan.installer.data.app.model.exception.InstallFailedVerificationTimeoutException -> stringResource(R.string.exception_install_failed_verification_timeout)
                            is com.rosan.installer.data.app.model.exception.InstallFailedVerificationFailureException -> stringResource(R.string.exception_install_failed_verification_failure)
                            is com.rosan.installer.data.app.model.exception.InstallFailedPackageChangedException -> stringResource(R.string.exception_install_failed_package_changed)
                            is com.rosan.installer.data.app.model.exception.InstallFailedUidChangedException -> stringResource(R.string.exception_install_failed_uid_changed)
                            is com.rosan.installer.data.app.model.exception.InstallFailedVersionDowngradeException -> stringResource(R.string.exception_install_failed_version_downgrade)

                            is ShizukuNotWorkException -> stringResource(R.string.exception_shizuku_not_work)
                            is DhizukuNotWorkException -> stringResource(R.string.exception_dhizuku_not_work)
                            is RootNotWorkException -> stringResource(R.string.exception_root_not_work)
                            is AppProcessNotWorkException -> stringResource(R.string.exception_app_process_not_work)
                            else -> stringResource(R.string.exception_install_failed_unknown)
                        }
                        Text(text, fontWeight = FontWeight.Bold)
                    }
                    item {
                        SelectionContainer {
                            Text(installer.error.stackTraceToString().trim())
                        }
                    }
                }
            }
        }
    }