package com.ntduc.apputils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.ntduc.apputils.model.BaseApp
import java.io.File
import java.io.IOException


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

private const val PACKAGE_INSTALLED_ACTION =
    "com.android.apis.content.SESSION_API_PACKAGE_INSTALLED"

fun Context.installApk(path : String) {
    var session: PackageInstaller.Session? = null
    try {
        val packageInstaller = packageManager.packageInstaller
        val params = SessionParams(
            SessionParams.MODE_FULL_INSTALL
        )
        val sessionId = packageInstaller.createSession(params)
        session = packageInstaller.openSession(sessionId)
        addApkToInstallSession("app-debug.apk", session)
        // Create an install status receiver.
        val intent = Intent(this, this::class.java)
        intent.action = PACKAGE_INSTALLED_ACTION
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val statusReceiver = pendingIntent.intentSender
        // Commit the session (this will start the installation workflow).
        session.commit(statusReceiver)
    } catch (e: IOException) {
        throw RuntimeException("Couldn't install package", e)
    } catch (e: RuntimeException) {
        session?.abandon()
        throw e
    }
}

@Throws(IOException::class)
private fun Context.addApkToInstallSession(assetName: String, session: PackageInstaller.Session) {
    // It's recommended to pass the file size to openWrite(). Otherwise installation may fail
    // if the disk is almost full.
    session.openWrite("package", 0, -1).use { packageInSession ->
        assets.open(assetName).use { inputStream ->
            val buffer = ByteArray(16384)
            var n: Int
            while (inputStream.read(buffer).also { n = it } >= 0) {
                packageInSession.write(buffer, 0, n)
            }
        }
    }
}

/**
 * Returns a Uri pointing to the APK to install.
 */
private fun Context.getApkUri(assetName: String, authority: String): Uri? {
    // Before N, a MODE_WORLD_READABLE file could be passed via the ACTION_INSTALL_PACKAGE
    // Intent. Since N, MODE_WORLD_READABLE files are forbidden, and a FileProvider is
    // recommended.
    val useFileProvider = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    // Copy the given asset out into a file so that it can be installed.
    // Returns the path to the file.
    val tempFilename = "tmp.apk"
    val buffer = ByteArray(16384)
    val fileMode = if (useFileProvider) Context.MODE_PRIVATE else Context.MODE_WORLD_READABLE
    try {
        assets.open(assetName).use { inputStream ->
            openFileOutput(tempFilename, fileMode).use { fout ->
                var n: Int
                while (inputStream.read(buffer).also { n = it } >= 0) {
                    fout.write(buffer, 0, n)
                }
            }
        }
    } catch (e: IOException) {
        Log.i("InstallApk", "Failed to write temporary APK file", e)
    }
    return if (useFileProvider) {
        val toInstall = File(this.filesDir, tempFilename)
        FileProvider.getUriForFile(
            this, authority, toInstall
        )
    } else {
        Uri.fromFile(getFileStreamPath(tempFilename))
    }
}