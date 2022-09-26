package com.ntduc.apputils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.ntduc.apputils.model.BaseApp
import java.io.*

fun Context.getApps(
    isSystem: Boolean = false
): List<BaseApp> {
    val apps = ArrayList<BaseApp>()
    val packs = this.packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    for (p in packs) {
        if (isSystem || !isSystemApplication(p)){
            val newInfo = BaseApp()
            newInfo.name = p.loadLabel(this.packageManager).toString()
            newInfo.packageName = p.packageName
            newInfo.icon = p.loadIcon(this.packageManager)
            newInfo.storage = File(p.sourceDir).length()
            apps.add(newInfo)
        }
    }
    return apps
}

private fun isSystemApplication(appInfo: ApplicationInfo): Boolean {
    return appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
}