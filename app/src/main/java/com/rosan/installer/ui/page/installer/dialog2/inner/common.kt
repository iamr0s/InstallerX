package com.rosan.installer.ui.page.installer.dialog2.inner

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
import com.rosan.installer.data.app.model.entity.error.*
import com.rosan.installer.data.installer.repo.InstallerRepo
import com.rosan.installer.ui.page.installer.dialog2.DialogViewModel

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
                            is InstallFailedAlreadyExistsException -> stringResource(R.string.install_failed_already_exists)
                            is InstallFailedInvalidAPKException -> stringResource(R.string.install_failed_invalid_apk)
                            is InstallFailedInvalidURIException -> stringResource(R.string.install_failed_invalid_uri)
                            is InstallFailedInsufficientStorageException -> stringResource(R.string.install_failed_insufficient_storage)
                            is InstallFailedDuplicatePackageException -> stringResource(R.string.install_failed_duplicate_package)
                            is InstallFailedNoSharedUserException -> stringResource(R.string.install_failed_no_shared_user)
                            is InstallFailedUpdateIncompatibleException -> stringResource(R.string.install_failed_update_incompatible)
                            is InstallFailedSharedUserIncompatibleException -> stringResource(R.string.install_failed_shared_user_incompatible)
                            is InstallFailedMissingSharedLibraryException -> stringResource(R.string.install_failed_missing_shared_library)
                            is InstallFailedReplaceCouldntDeleteException -> stringResource(R.string.install_failed_replace_couldnt_delete)
                            is InstallFailedDexoptException -> stringResource(R.string.install_failed_dexopt)
                            is InstallFailedOlderSdkException -> stringResource(R.string.install_failed_older_sdk)
                            is InstallFailedConflictingProviderException -> stringResource(R.string.install_failed_conflicting_provider)
                            is InstallFailedNewerSDKException -> stringResource(R.string.install_failed_newer_sdk)
                            is InstallFailedTestOnlyException -> stringResource(R.string.install_failed_test_only)
                            is InstallFailedCpuAbiIncompatibleException -> stringResource(R.string.install_failed_cpu_abi_incompatible)
                            is InstallFailedMissingFeatureException -> stringResource(R.string.install_failed_missing_feature)
                            is InstallFailedContainerErrorException -> stringResource(R.string.install_failed_container_error)
                            is InstallFailedInvalidInstallLocationException -> stringResource(R.string.install_failed_invalid_install_location)
                            is InstallFailedMediaUnavailableException -> stringResource(R.string.install_failed_media_unavailable)
                            is InstallFailedVerificationTimeoutException -> stringResource(R.string.install_failed_verification_timeout)
                            is InstallFailedVerificationFailureException -> stringResource(R.string.install_failed_verification_failure)
                            is InstallFailedPackageChangedException -> stringResource(R.string.install_failed_package_changed)
                            is InstallFailedUidChangedException -> stringResource(R.string.install_failed_uid_changed)
                            is InstallFailedVersionDowngradeException -> stringResource(R.string.install_failed_version_downgrade)
                            else -> stringResource(R.string.install_failed_unknown)
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