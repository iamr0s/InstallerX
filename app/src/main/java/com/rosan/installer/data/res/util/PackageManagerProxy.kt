package com.rosan.installer.data.res.util

import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.ChangedPackages
import android.content.pm.FeatureInfo
import android.content.pm.InstrumentationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.content.pm.PermissionGroupInfo
import android.content.pm.PermissionInfo
import android.content.pm.ProviderInfo
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.content.pm.SharedLibraryInfo
import android.content.pm.VersionedPackage
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.UserHandle

class PackageManagerProxy : PackageManager() {
    @Deprecated("Deprecated in Java")
    override fun getPackageInfo(p0: String, p1: Int): PackageInfo {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getPackageInfo(p0: VersionedPackage, p1: Int): PackageInfo {
        TODO("Not yet implemented")
    }

    override fun currentToCanonicalPackageNames(p0: Array<out String>): Array<String> {
        TODO("Not yet implemented")
    }

    override fun canonicalToCurrentPackageNames(p0: Array<out String>): Array<String> {
        TODO("Not yet implemented")
    }

    override fun getLaunchIntentForPackage(p0: String): Intent? {
        TODO("Not yet implemented")
    }

    override fun getLeanbackLaunchIntentForPackage(p0: String): Intent? {
        TODO("Not yet implemented")
    }

    override fun getPackageGids(p0: String): IntArray {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getPackageGids(p0: String, p1: Int): IntArray {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getPackageUid(p0: String, p1: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getPermissionInfo(p0: String, p1: Int): PermissionInfo {
        TODO("Not yet implemented")
    }

    override fun queryPermissionsByGroup(p0: String?, p1: Int): MutableList<PermissionInfo> {
        TODO("Not yet implemented")
    }

    override fun getPermissionGroupInfo(p0: String, p1: Int): PermissionGroupInfo {
        TODO("Not yet implemented")
    }

    override fun getAllPermissionGroups(p0: Int): MutableList<PermissionGroupInfo> {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getApplicationInfo(p0: String, p1: Int): ApplicationInfo {
        TODO("Not yet implemented")
    }

    override fun getActivityInfo(p0: ComponentName, p1: Int): ActivityInfo {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getReceiverInfo(p0: ComponentName, p1: Int): ActivityInfo {
        TODO("Not yet implemented")
    }

    override fun getServiceInfo(p0: ComponentName, p1: Int): ServiceInfo {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getProviderInfo(p0: ComponentName, p1: Int): ProviderInfo {
        TODO("Not yet implemented")
    }

    override fun getInstalledPackages(p0: Int): MutableList<PackageInfo> {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getPackagesHoldingPermissions(
        p0: Array<out String>,
        p1: Int
    ): MutableList<PackageInfo> {
        TODO("Not yet implemented")
    }

    override fun checkPermission(p0: String, p1: String): Int {
        TODO("Not yet implemented")
    }

    override fun isPermissionRevokedByPolicy(p0: String, p1: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun addPermission(p0: PermissionInfo): Boolean {
        TODO("Not yet implemented")
    }

    override fun addPermissionAsync(p0: PermissionInfo): Boolean {
        TODO("Not yet implemented")
    }

    override fun removePermission(p0: String) {
        TODO("Not yet implemented")
    }

    override fun checkSignatures(p0: String, p1: String): Int {
        TODO("Not yet implemented")
    }

    override fun checkSignatures(p0: Int, p1: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getPackagesForUid(p0: Int): Array<String>? {
        TODO("Not yet implemented")
    }

    override fun getNameForUid(p0: Int): String? {
        TODO("Not yet implemented")
    }

    override fun getInstalledApplications(p0: Int): MutableList<ApplicationInfo> {
        TODO("Not yet implemented")
    }

    override fun isInstantApp(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isInstantApp(p0: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getInstantAppCookieMaxBytes(): Int {
        TODO("Not yet implemented")
    }

    override fun getInstantAppCookie(): ByteArray {
        TODO("Not yet implemented")
    }

    override fun clearInstantAppCookie() {
        TODO("Not yet implemented")
    }

    override fun updateInstantAppCookie(p0: ByteArray?) {
        TODO("Not yet implemented")
    }

    override fun getSystemSharedLibraryNames(): Array<String>? {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getSharedLibraries(p0: Int): MutableList<SharedLibraryInfo> {
        TODO("Not yet implemented")
    }

    override fun getChangedPackages(p0: Int): ChangedPackages? {
        TODO("Not yet implemented")
    }

    override fun getSystemAvailableFeatures(): Array<FeatureInfo> {
        TODO("Not yet implemented")
    }

    override fun hasSystemFeature(p0: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun hasSystemFeature(p0: String, p1: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun resolveActivity(p0: Intent, p1: Int): ResolveInfo? {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun queryIntentActivities(p0: Intent, p1: Int): MutableList<ResolveInfo> {
        TODO("Not yet implemented")
    }

    override fun queryIntentActivityOptions(
        p0: ComponentName?,
        p1: Array<out Intent>?,
        p2: Intent,
        p3: Int
    ): MutableList<ResolveInfo> {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun queryBroadcastReceivers(p0: Intent, p1: Int): MutableList<ResolveInfo> {
        TODO("Not yet implemented")
    }

    override fun resolveService(p0: Intent, p1: Int): ResolveInfo? {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun queryIntentServices(p0: Intent, p1: Int): MutableList<ResolveInfo> {
        TODO("Not yet implemented")
    }

    override fun queryIntentContentProviders(p0: Intent, p1: Int): MutableList<ResolveInfo> {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun resolveContentProvider(p0: String, p1: Int): ProviderInfo? {
        TODO("Not yet implemented")
    }

    override fun queryContentProviders(p0: String?, p1: Int, p2: Int): MutableList<ProviderInfo> {
        TODO("Not yet implemented")
    }

    override fun getInstrumentationInfo(p0: ComponentName, p1: Int): InstrumentationInfo {
        TODO("Not yet implemented")
    }

    override fun queryInstrumentation(p0: String, p1: Int): MutableList<InstrumentationInfo> {
        TODO("Not yet implemented")
    }

    override fun getDrawable(p0: String, p1: Int, p2: ApplicationInfo?): Drawable? {
        TODO("Not yet implemented")
    }

    override fun getActivityIcon(p0: ComponentName): Drawable {
        TODO("Not yet implemented")
    }

    override fun getActivityIcon(p0: Intent): Drawable {
        TODO("Not yet implemented")
    }

    override fun getActivityBanner(p0: ComponentName): Drawable? {
        TODO("Not yet implemented")
    }

    override fun getActivityBanner(p0: Intent): Drawable? {
        TODO("Not yet implemented")
    }

    override fun getDefaultActivityIcon(): Drawable {
        TODO("Not yet implemented")
    }

    override fun getApplicationIcon(p0: ApplicationInfo): Drawable {
        TODO("Not yet implemented")
    }

    override fun getApplicationIcon(p0: String): Drawable {
        TODO("Not yet implemented")
    }

    override fun getApplicationBanner(p0: ApplicationInfo): Drawable? {
        TODO("Not yet implemented")
    }

    override fun getApplicationBanner(p0: String): Drawable? {
        TODO("Not yet implemented")
    }

    override fun getActivityLogo(p0: ComponentName): Drawable? {
        TODO("Not yet implemented")
    }

    override fun getActivityLogo(p0: Intent): Drawable? {
        TODO("Not yet implemented")
    }

    override fun getApplicationLogo(p0: ApplicationInfo): Drawable? {
        TODO("Not yet implemented")
    }

    override fun getApplicationLogo(p0: String): Drawable? {
        TODO("Not yet implemented")
    }

    override fun getUserBadgedIcon(p0: Drawable, p1: UserHandle): Drawable {
        TODO("Not yet implemented")
    }

    override fun getUserBadgedDrawableForDensity(
        p0: Drawable,
        p1: UserHandle,
        p2: Rect?,
        p3: Int
    ): Drawable {
        TODO("Not yet implemented")
    }

    override fun getUserBadgedLabel(p0: CharSequence, p1: UserHandle): CharSequence {
        TODO("Not yet implemented")
    }

    override fun getText(p0: String, p1: Int, p2: ApplicationInfo?): CharSequence? {
        TODO("Not yet implemented")
    }

    override fun getXml(p0: String, p1: Int, p2: ApplicationInfo?): XmlResourceParser? {
        TODO("Not yet implemented")
    }

    override fun getApplicationLabel(p0: ApplicationInfo): CharSequence {
        TODO("Not yet implemented")
    }

    override fun getResourcesForActivity(p0: ComponentName): Resources {
        TODO("Not yet implemented")
    }

    override fun getResourcesForApplication(p0: ApplicationInfo): Resources {
        TODO("Not yet implemented")
    }

    override fun getResourcesForApplication(p0: String): Resources {
        TODO("Not yet implemented")
    }

    override fun verifyPendingInstall(p0: Int, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun extendVerificationTimeout(p0: Int, p1: Int, p2: Long) {
        TODO("Not yet implemented")
    }

    override fun setInstallerPackageName(p0: String, p1: String?) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getInstallerPackageName(p0: String): String? {
        TODO("Not yet implemented")
    }

    override fun addPackageToPreferred(p0: String) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun removePackageFromPreferred(p0: String) {
        TODO("Not yet implemented")
    }

    override fun getPreferredPackages(p0: Int): MutableList<PackageInfo> {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun addPreferredActivity(
        p0: IntentFilter,
        p1: Int,
        p2: Array<out ComponentName>?,
        p3: ComponentName
    ) {
        TODO("Not yet implemented")
    }

    override fun clearPackagePreferredActivities(p0: String) {
        TODO("Not yet implemented")
    }

    @Deprecated("Deprecated in Java")
    override fun getPreferredActivities(
        p0: MutableList<IntentFilter>,
        p1: MutableList<ComponentName>,
        p2: String?
    ): Int {
        TODO("Not yet implemented")
    }

    override fun setComponentEnabledSetting(p0: ComponentName, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun getComponentEnabledSetting(p0: ComponentName): Int {
        TODO("Not yet implemented")
    }

    override fun setApplicationEnabledSetting(p0: String, p1: Int, p2: Int) {
        TODO("Not yet implemented")
    }

    override fun getApplicationEnabledSetting(p0: String): Int {
        TODO("Not yet implemented")
    }

    override fun isSafeMode(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setApplicationCategoryHint(p0: String, p1: Int) {
        TODO("Not yet implemented")
    }

    override fun getPackageInstaller(): PackageInstaller {
        TODO("Not yet implemented")
    }

    override fun canRequestPackageInstalls(): Boolean {
        TODO("Not yet implemented")
    }
}