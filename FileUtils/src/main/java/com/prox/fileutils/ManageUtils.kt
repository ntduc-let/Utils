package com.prox.fileutils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

fun Context.renameFile(fileOld: File, nameNew: String, onCompleted: () -> Unit): Boolean {
    try {
        val pathNew = "${fileOld.parentFile?.path}/${nameNew}.${fileOld.extension}"
        val fileNew = File(pathNew)
        if (fileOld.renameTo(fileNew)) {
            var index = 0
            MediaScannerConnection.scanFile(
                this, listOf(fileOld.path, fileNew.path).toTypedArray(), null
            ) { _, _ ->
                index++
                if (index == listOf(fileOld.path, fileNew.path).size) {
                    onCompleted()
                }
            }
            return true
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

fun Context.deleteFiles(files: List<File>, onCompleted: () -> Unit): Boolean {
    var index = 0
    for (file in files) {
        if (file.delete()) {
            MediaScannerConnection.scanFile(
                this, listOf(file.path).toTypedArray(), null
            ) { _, _ ->
                index++
                if (index == files.size) {
                    onCompleted()
                }
            }
        } else {
            return false
        }
    }
    return true
}

@SuppressLint("QueryPermissionsNeeded")
fun Context.shareFile(file: File, authority: String) {
    val uri = FileProvider.getUriForFile(this, authority, file)
    val intentShareFile = Intent(Intent.ACTION_SEND)
    val titleFull = file.name
    intentShareFile.type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)
    intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
    intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val chooser = Intent.createChooser(intentShareFile, titleFull)
    val resInfoList =
        packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)
    for (resolveInfo in resInfoList) {
        val packageName = resolveInfo.activityInfo.packageName
        grantUriPermission(
            packageName,
            uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
    startActivity(chooser)
}