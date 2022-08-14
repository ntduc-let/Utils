package com.prox.fileutils

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.Nullable
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.util.*

fun File.deleteAll() {
    if (isFile && exists()) {
        delete()
        return
    }
    if (isDirectory) {
        val files = listFiles()
        if (files == null || files.isEmpty()) {
            delete()
            return
        }
        files.forEach { it.deleteAll() }
        delete()
    }
}

fun File.readToString(): String {
    var text: String
    open().use { inpS ->
        inpS.bufferedReader().use {
            text = it.readText()
            it.close()
        }
        inpS.close()
    }
    return text
}

fun File.open(): InputStream = FileInputStream(this)

fun File.move(dest: File) {
    if (isFile)
        renameTo(dest)
    else
        moveDirectory(dest)
}

fun File.copy(dest: File) {
    if (isDirectory)
        copyDirectory(dest)
    else
        copyFile(dest)
}

fun File.isImage(): Boolean {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    return try {
        val bitmap = BitmapFactory.decodeFile(absolutePath, options)
        val result = options.outWidth != -1 && options.outHeight != -1
        bitmap.recycle()
        return result
    } catch (e: Exception) {
        false
    }
}

fun File.toByteArray(): ByteArray {
    val bos = ByteArrayOutputStream(this.length().toInt())
    val input = FileInputStream(this)
    val size = 1024
    val buffer = ByteArray(size)
    var len = input.read(buffer, 0, size)
    while (len != -1) {
        bos.write(buffer, 0, len)
        len = input.read(buffer, 0, size)
    }
    input.close()
    bos.close()
    return bos.toByteArray()
}

fun File.copyFromInputStream(inputStream: InputStream) =
    inputStream.use { input -> outputStream().use { output -> input.copyTo(output) } }

fun Context.deleteCache() {
    try {
        val dir = this.cacheDir
        if (dir != null && dir.isDirectory) {
            deleteDir(dir)
        }
    } catch (e: Exception) {
    }

}

fun deleteDir(@Nullable dir: File?): Boolean {
    if (dir != null && dir.isDirectory) {
        val children = dir.list()
        if (children.isNullOrEmpty()) return false
        for (i in children.indices) {
            val success = deleteDir(File(dir, children[i]))
            if (!success) {
                return false
            }
        }
    }
    return dir?.delete() == true
}


fun String.toUri(): Uri {
    return Uri.parse(this)
}

fun File.toUri(): Uri {
    return Uri.fromFile(this)
}


fun File.copyInputStreamToFile(inputStream: InputStream) {
    inputStream.use { input ->
        this.outputStream().use { fileOut ->
            input.copyTo(fileOut)
        }
    }
}

fun ContentResolver.fileSize(uri: Uri): Long? {
    return openFileDescriptor(uri, "r")?.statSize
}

fun downloadFile(urlPath: String, localPath: String, callback: (Uri?) -> Unit = {}): Uri? {
    var uri: Uri? = null
    val connection = URL(urlPath).openConnection() as HttpURLConnection

    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
        uri = Uri.fromFile(connection.inputStream.outAsFile(localPath.toFile()))
    }
    connection.disconnect()
    if (uri is Uri) {
        callback(uri)
    } else {
        callback(null)
    }
    return uri
}

fun downloadFileWithProgress(urlPath: String, localPath: String,
                             connectionCallBack: (responseCode: Int) -> Unit = {},
                             onError: (Exception) -> Unit = {}, progress: (Int) -> Unit = {}, callback: (Uri?) -> Unit = {}) {
    val uri = localPath.toFile().toUri()
    val connection = URL(urlPath).openConnection() as HttpURLConnection
    val input = connection.inputStream
    val output = FileOutputStream(uri.toFile())
    try {
        connection.connect()

        val responseCode = connection.responseCode
        connectionCallBack(responseCode)
        if (responseCode != HttpURLConnection.HTTP_OK) {
            return
        }

        val fileLength = connection.contentLength
        val data = ByteArray(4096)
        var total: Long = 0
        var count: Int
        while (input.read(data).also { count = it } != -1) {
            total += count.toLong()
            if (fileLength > 0)
                progress((total * 100 / fileLength).toInt())
            output.write(data, 0, count)
        }
    } catch (e: Exception) {
        onError(e)
        return
    } finally {
        tryOrIgnore {
            output.close()
            input?.close()
        }
        connection.disconnect()
    }
    callback(uri)
}


fun String.toFile() = File(this)

fun saveFile(fullPath: String, content: String): File =
    fullPath.toFile().apply {
        writeText(content, Charset.defaultCharset())
    }


fun InputStream.getString(): String = this.bufferedReader().readText()

fun InputStream.outAsFile(file: File): File {
    file.createNewFile()

    use { input ->
        file.outputStream().use { fileOut ->
            input.copyTo(fileOut)
        }
    }
    return file
}

fun InputStream.outAsBitmap(): Bitmap? = use {
    BitmapFactory.decodeStream(it)
}

/**
 * Gets an uri of file
 */
fun File.getUriFromFile(context: Context, authority: String): Uri {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
        FileProvider.getUriForFile(context, authority, this)
    } else {
        Uri.fromFile(this)
    }
}


/**
 * Gets an uri of file
 */
fun Context.getUriFromFile(file: File, authority: String): Uri {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
        FileProvider.getUriForFile(this, authority, file)
    } else {
        Uri.fromFile(file)
    }
}


/**
 * Checks and returns if there's a valid directory with given path
 */
fun String.getAsDirectory(): File? {
    val directory = File(Environment.getRootDirectory(), this)
    return if (directory.exists()) {
        directory
    } else {
        null
    }
}

/**
 * Gets all files in given directory
 */
fun File.getFiles(): List<File> {
    val inFiles = ArrayList<File>()
    val files = this.listFiles()
    if (files != null) {
        for (file in files) {
            if (file.isDirectory) {
                inFiles.addAll(file.getFiles())
            } else {
                inFiles.add(file)
            }
        }
    }
    return inFiles
}

/**
 * Gets the file count of given directory
 */
fun File.getFileCount() = getFiles().size

/**
 * Calculates the folder size
 */
fun File.getFolderSize(): Long {
    var size: Long = 0
    if (isDirectory) {
        val files = listFiles()
        if (files != null) {
            for (file in files) {
                size += if (file.isDirectory) {
                    file.getFolderSize()
                } else {
                    file.length()
                }
            }
        } else {
            size = 0
        }
    } else {
        size = length()
    }

    return size
}

// Private Methods
private fun File.copyFile(dest: File) {
    var fi: FileInputStream? = null
    var fo: FileOutputStream? = null
    var ic: FileChannel? = null
    var oc: FileChannel? = null
    try {
        if (!dest.exists()) {
            dest.createNewFile()
        }
        fi = FileInputStream(this)
        fo = FileOutputStream(dest)
        ic = fi.channel
        oc = fo.channel
        ic.transferTo(0, ic.size(), oc)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        fi?.close()
        fo?.close()
        ic?.close()
        oc?.close()
    }
}


private fun File.copyDirectory(dest: File) {
    if (!dest.exists()) {
        dest.mkdirs()
    }
    val files = listFiles()
    files?.forEach {
        if (it.isFile) {
            it.copyFile(File("${dest.absolutePath}/${it.name}"))
        }
        if (it.isDirectory) {
            val dirSrc = File("$absolutePath/${it.name}")
            val dirDest = File("${dest.absolutePath}/${it.name}")
            dirSrc.copyDirectory(dirDest)
        }
    }
}


private fun File.moveDirectory(dest: File) {
    copyDirectory(dest)
    deleteAll()
}

private fun tryOrIgnore(runnable: () -> Unit) = try {
    runnable()
} catch (e: Exception) {
    e.printStackTrace()
}