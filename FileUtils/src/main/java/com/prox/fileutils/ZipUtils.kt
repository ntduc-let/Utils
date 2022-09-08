package com.prox.fileutils

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun File.createZip(paths: List<String>, bufferSize: Int = 2048): String? {
    try {
        var origin: BufferedInputStream
        val dest = FileOutputStream(this)
        val out = ZipOutputStream(BufferedOutputStream(dest))

        val data = ByteArray(bufferSize)
        for (path in paths) {
            val fi = FileInputStream(path)
            origin = BufferedInputStream(fi, bufferSize)

            val entry = ZipEntry(
                path.substring(
                    path.lastIndexOf("/") + 1
                )
            )
            out.putNextEntry(entry)
            val count: Int = origin.read(data, 0, bufferSize)

            while (count != -1) {
                out.write(data, 0, count)
            }
            origin.close()
        }

        out.close()
        return this.toString()
    } catch (ignored: Exception) {
        ignored.printStackTrace()
    }

    return null
}

//@Throws(IOException::class)
//fun zipFiles(
//    srcFilePaths: Collection<String>?,
//    zipFilePath: String?,
//    comment: String?
//): Boolean {
//    if (srcFilePaths == null || zipFilePath == null) return false
//    var zos: ZipOutputStream? = null
//    try {
//        zos = ZipOutputStream(FileOutputStream(zipFilePath))
//        for (srcFile in srcFilePaths) {
//            if (!createZipFile(
//                    getFileByPath(srcFile)!!,
//                    "",
//                    zos,
//                    comment
//                )
//            ) return false
//        }
//        return true
//    } finally {
//        if (zos != null) {
//            zos.finish()
//            zos.close()
//        }
//    }
//}