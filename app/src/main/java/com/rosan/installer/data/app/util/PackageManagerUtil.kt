package com.rosan.installer.data.app.util

import android.content.Intent
import android.content.pm.PackageInstaller
import com.rosan.installer.data.app.model.entity.error.*

class PackageManagerUtil {
    companion object {
        const val INSTALL_FAILED_ALREADY_EXISTS = -1

        const val INSTALL_FAILED_INVALID_APK = -2

        const val INSTALL_FAILED_INVALID_URI = -3

        const val INSTALL_FAILED_INSUFFICIENT_STORAGE = -4

        const val INSTALL_FAILED_DUPLICATE_PACKAGE = -5

        const val INSTALL_FAILED_NO_SHARED_USER = -6

        const val INSTALL_FAILED_UPDATE_INCOMPATIBLE = -7

        const val INSTALL_FAILED_SHARED_USER_INCOMPATIBLE = -8

        const val INSTALL_FAILED_MISSING_SHARED_LIBRARY = -9

        const val INSTALL_FAILED_REPLACE_COULDNT_DELETE = -10

        const val INSTALL_FAILED_DEXOPT = -11

        const val INSTALL_FAILED_OLDER_SDK = -12

        const val INSTALL_FAILED_CONFLICTING_PROVIDER = -13

        const val INSTALL_FAILED_NEWER_SDK = -14

        const val INSTALL_FAILED_TEST_ONLY = -15

        const val INSTALL_FAILED_CPU_ABI_INCOMPATIBLE = -16

        const val INSTALL_FAILED_MISSING_FEATURE = -17

        const val INSTALL_FAILED_CONTAINER_ERROR = -18

        const val INSTALL_FAILED_INVALID_INSTALL_LOCATION = -19

        const val INSTALL_FAILED_MEDIA_UNAVAILABLE = -20

        const val INSTALL_FAILED_VERIFICATION_TIMEOUT = -21

        const val INSTALL_FAILED_VERIFICATION_FAILURE = -22

        const val INSTALL_FAILED_PACKAGE_CHANGED = -23

        const val INSTALL_FAILED_UID_CHANGED = -24

        const val INSTALL_FAILED_VERSION_DOWNGRADE = -25

        fun installResultVerify(intent: Intent) {
            val status =
                intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
            if (status == PackageInstaller.STATUS_SUCCESS) return
            val legacyStatus = intent.getIntExtra(
                PackageInstallerUtil.EXTRA_LEGACY_STATUS, PackageInstaller.STATUS_FAILURE
            )
            val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
            val ecpMsg = "Install Failure $status [$msg]"
            throw when (legacyStatus) {
                INSTALL_FAILED_ALREADY_EXISTS -> InstallFailedAlreadyExistsException(ecpMsg)
                INSTALL_FAILED_INVALID_APK -> InstallFailedInvalidAPKException(ecpMsg)
                INSTALL_FAILED_INVALID_URI -> InstallFailedInvalidURIException(ecpMsg)
                INSTALL_FAILED_INSUFFICIENT_STORAGE -> InstallFailedInsufficientStorageException(
                    ecpMsg
                )
                INSTALL_FAILED_DUPLICATE_PACKAGE -> InstallFailedDuplicatePackageException(ecpMsg)
                INSTALL_FAILED_NO_SHARED_USER -> InstallFailedNoSharedUserException(ecpMsg)
                INSTALL_FAILED_UPDATE_INCOMPATIBLE -> InstallFailedUpdateIncompatibleException(
                    ecpMsg
                )
                INSTALL_FAILED_SHARED_USER_INCOMPATIBLE -> InstallFailedSharedUserIncompatibleException(
                    ecpMsg
                )
                INSTALL_FAILED_MISSING_SHARED_LIBRARY -> InstallFailedMissingSharedLibraryException(
                    ecpMsg
                )
                INSTALL_FAILED_REPLACE_COULDNT_DELETE -> InstallFailedReplaceCouldntDeleteException(
                    ecpMsg
                )
                INSTALL_FAILED_DEXOPT -> InstallFailedDexoptException(ecpMsg)
                INSTALL_FAILED_OLDER_SDK -> InstallFailedOlderSdkException(ecpMsg)
                INSTALL_FAILED_CONFLICTING_PROVIDER -> InstallFailedConflictingProviderException(
                    ecpMsg
                )
                INSTALL_FAILED_NEWER_SDK -> InstallFailedNewerSDKException(ecpMsg)
                INSTALL_FAILED_TEST_ONLY -> InstallFailedTestOnlyException(ecpMsg)
                INSTALL_FAILED_CPU_ABI_INCOMPATIBLE -> InstallFailedCpuAbiIncompatibleException(
                    ecpMsg
                )
                INSTALL_FAILED_MISSING_FEATURE -> InstallFailedMissingFeatureException(ecpMsg)
                INSTALL_FAILED_CONTAINER_ERROR -> InstallFailedContainerErrorException(ecpMsg)
                INSTALL_FAILED_INVALID_INSTALL_LOCATION -> InstallFailedInvalidInstallLocationException(
                    ecpMsg
                )
                INSTALL_FAILED_MEDIA_UNAVAILABLE -> InstallFailedMediaUnavailableException(ecpMsg)
                INSTALL_FAILED_VERIFICATION_TIMEOUT -> InstallFailedVerificationTimeoutException(
                    ecpMsg
                )
                INSTALL_FAILED_VERIFICATION_FAILURE -> InstallFailedVerificationFailureException(
                    ecpMsg
                )
                INSTALL_FAILED_PACKAGE_CHANGED -> InstallFailedPackageChangedException(ecpMsg)
                INSTALL_FAILED_UID_CHANGED -> InstallFailedUidChangedException(ecpMsg)
                INSTALL_FAILED_VERSION_DOWNGRADE -> InstallFailedVersionDowngradeException(ecpMsg)
                else -> IllegalStateException(ecpMsg)
            }
        }
    }
}