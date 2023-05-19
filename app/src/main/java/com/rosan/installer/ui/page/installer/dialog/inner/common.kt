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
import com.rosan.installer.data.app.model.entity.error.InstallFailedAlreadyExistsException
import com.rosan.installer.data.app.model.entity.error.InstallFailedConflictingProviderException
import com.rosan.installer.data.app.model.entity.error.InstallFailedContainerErrorException
import com.rosan.installer.data.app.model.entity.error.InstallFailedCpuAbiIncompatibleException
import com.rosan.installer.data.app.model.entity.error.InstallFailedDexoptException
import com.rosan.installer.data.app.model.entity.error.InstallFailedDuplicatePackageException
import com.rosan.installer.data.app.model.entity.error.InstallFailedInsufficientStorageException
import com.rosan.installer.data.app.model.entity.error.InstallFailedInvalidAPKException
import com.rosan.installer.data.app.model.entity.error.InstallFailedInvalidInstallLocationException
import com.rosan.installer.data.app.model.entity.error.InstallFailedInvalidURIException
import com.rosan.installer.data.app.model.entity.error.InstallFailedMediaUnavailableException
import com.rosan.installer.data.app.model.entity.error.InstallFailedMissingFeatureException
import com.rosan.installer.data.app.model.entity.error.InstallFailedMissingSharedLibraryException
import com.rosan.installer.data.app.model.entity.error.InstallFailedNewerSDKException
import com.rosan.installer.data.app.model.entity.error.InstallFailedNoSharedUserException
import com.rosan.installer.data.app.model.entity.error.InstallFailedOlderSdkException
import com.rosan.installer.data.app.model.entity.error.InstallFailedPackageChangedException
import com.rosan.installer.data.app.model.entity.error.InstallFailedReplaceCouldntDeleteException
import com.rosan.installer.data.app.model.entity.error.InstallFailedSharedUserIncompatibleException
import com.rosan.installer.data.app.model.entity.error.InstallFailedTestOnlyException
import com.rosan.installer.data.app.model.entity.error.InstallFailedUidChangedException
import com.rosan.installer.data.app.model.entity.error.InstallFailedUpdateIncompatibleException
import com.rosan.installer.data.app.model.entity.error.InstallFailedVerificationFailureException
import com.rosan.installer.data.app.model.entity.error.InstallFailedVerificationTimeoutException
import com.rosan.installer.data.app.model.entity.error.InstallFailedVersionDowngradeException
import com.rosan.installer.data.app.model.exception.RootNotWorkException
import com.rosan.installer.data.console.model.exception.ShizukuNotWorkException
import com.rosan.installer.data.installer.repo.InstallerRepo
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
                            is InstallFailedAlreadyExistsException -> stringResource(R.string.exception_install_failed_already_exists)
                            is InstallFailedInvalidAPKException -> stringResource(R.string.exception_install_failed_invalid_apk)
                            is InstallFailedInvalidURIException -> stringResource(R.string.exception_install_failed_invalid_uri)
                            is InstallFailedInsufficientStorageException -> stringResource(R.string.exception_install_failed_insufficient_storage)
                            is InstallFailedDuplicatePackageException -> stringResource(R.string.exception_install_failed_duplicate_package)
                            is InstallFailedNoSharedUserException -> stringResource(R.string.exception_install_failed_no_shared_user)
                            is InstallFailedUpdateIncompatibleException -> stringResource(R.string.exception_install_failed_update_incompatible)
                            is InstallFailedSharedUserIncompatibleException -> stringResource(R.string.exception_install_failed_shared_user_incompatible)
                            is InstallFailedMissingSharedLibraryException -> stringResource(R.string.exception_install_failed_missing_shared_library)
                            is InstallFailedReplaceCouldntDeleteException -> stringResource(R.string.exception_install_failed_replace_couldnt_delete)
                            is InstallFailedDexoptException -> stringResource(R.string.exception_install_failed_dexopt)
                            is InstallFailedOlderSdkException -> stringResource(R.string.exception_install_failed_older_sdk)
                            is InstallFailedConflictingProviderException -> stringResource(R.string.exception_install_failed_conflicting_provider)
                            is InstallFailedNewerSDKException -> stringResource(R.string.exception_install_failed_newer_sdk)
                            is InstallFailedTestOnlyException -> stringResource(R.string.exception_install_failed_test_only)
                            is InstallFailedCpuAbiIncompatibleException -> stringResource(R.string.exception_install_failed_cpu_abi_incompatible)
                            is InstallFailedMissingFeatureException -> stringResource(R.string.exception_install_failed_missing_feature)
                            is InstallFailedContainerErrorException -> stringResource(R.string.exception_install_failed_container_error)
                            is InstallFailedInvalidInstallLocationException -> stringResource(R.string.exception_install_failed_invalid_install_location)
                            is InstallFailedMediaUnavailableException -> stringResource(R.string.exception_install_failed_media_unavailable)
                            is InstallFailedVerificationTimeoutException -> stringResource(R.string.exception_install_failed_verification_timeout)
                            is InstallFailedVerificationFailureException -> stringResource(R.string.exception_install_failed_verification_failure)
                            is InstallFailedPackageChangedException -> stringResource(R.string.exception_install_failed_package_changed)
                            is InstallFailedUidChangedException -> stringResource(R.string.exception_install_failed_uid_changed)
                            is InstallFailedVersionDowngradeException -> stringResource(R.string.exception_install_failed_version_downgrade)

                            is ShizukuNotWorkException -> stringResource(R.string.exception_shizuku_not_work)
                            is RootNotWorkException -> stringResource(R.string.exception_root_not_work)
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