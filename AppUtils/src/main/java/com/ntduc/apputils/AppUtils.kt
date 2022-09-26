package com.ntduc.apputils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import com.ntduc.apputils.model.BaseApp

@SuppressLint("QueryPermissionsNeeded")
fun Context.getApps(
    isSystem: Boolean = false
): List<BaseApp> {
    val apps = ArrayList<BaseApp>()
    val packs = this.packageManager.getInstalledApplications(0)

    for (pack in packs) {
        if (isSystem || !isSystemApplication(pack)) {
            val newInfo = BaseApp()
            try {
                newInfo.name = pack.loadLabel(this.packageManager).toString()
            } catch (_: Exception) {
            }
            newInfo.packageName = pack.packageName
            try {
                newInfo.icon = pack.loadIcon(this.packageManager)
            } catch (_: Exception) {
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    newInfo.category =
                        ApplicationInfo.getCategoryTitle(this, pack.category).toString()
                } catch (_: Exception) {
                }
            }
            newInfo.dataDir = pack.dataDir
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                newInfo.minSdkVersion = pack.minSdkVersion
            }
            newInfo.targetSdkVersion = pack.targetSdkVersion
            newInfo.processName = pack.processName
            newInfo.nativeLibraryDir = pack.nativeLibraryDir
            newInfo.publicSourceDir = pack.publicSourceDir
            newInfo.sourceDir = pack.sourceDir
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                newInfo.splitNames = pack.splitNames
            }
            newInfo.splitPublicSourceDirs = pack.splitPublicSourceDirs
            newInfo.splitSourceDirs = pack.splitSourceDirs
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                newInfo.storageUuid = pack.storageUuid
            }
            newInfo.taskAffinity = pack.taskAffinity
            newInfo.uid = pack.uid
            apps.add(newInfo)
        }
    }
    return apps
}

private fun isSystemApplication(appInfo: ApplicationInfo): Boolean {
    return appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
}