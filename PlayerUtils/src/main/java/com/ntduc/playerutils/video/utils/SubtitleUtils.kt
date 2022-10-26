package com.ntduc.playerutils.video.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.util.MimeTypes
import androidx.documentfile.provider.DocumentFile
import com.ibm.icu.text.CharsetDetector
import com.google.android.exoplayer2.MediaItem.SubtitleConfiguration
import com.google.android.exoplayer2.C
import com.ntduc.playerutils.video.player.SubtitleFetcher
import com.ntduc.playerutils.video.player.VideoPlayerActivity
import java.io.*
import java.lang.Exception
import java.lang.NullPointerException
import java.nio.charset.StandardCharsets
import java.util.*

internal object SubtitleUtils {
    fun getSubtitleMime(uri: Uri): String {
        val path = uri.path
        return if (path!!.endsWith(".ssa") || path.endsWith(".ass")) {
            MimeTypes.TEXT_SSA
        } else if (path.endsWith(".vtt")) {
            MimeTypes.TEXT_VTT
        } else if (path.endsWith(".ttml") || path.endsWith(".xml") || path.endsWith(".dfxp")) {
            MimeTypes.APPLICATION_TTML
        } else {
            MimeTypes.APPLICATION_SUBRIP
        }
    }

    fun getSubtitleLanguage(uri: Uri): String? {
        val path = uri.path!!.lowercase(Locale.getDefault())
        if (path.endsWith(".srt")) {
            val last = path.lastIndexOf(".")
            var prev = last
            for (i in last downTo 0) {
                prev = path.indexOf(".", i)
                if (prev != last) break
            }
            val len = last - prev
            if (len in 2..6) {
                // TODO: Validate lang
                return path.substring(prev + 1, last)
            }
        }
        return null
    }

    /*
    public static DocumentFile findUriInScope(DocumentFile documentFileTree, Uri uri) {
        for (DocumentFile file : documentFileTree.listFiles()) {
            if (file.isDirectory()) {
                final DocumentFile ret = findUriInScope(file, uri);
                if (ret != null)
                    return ret;
            } else {
                final Uri fileUri = file.getUri();
                if (fileUri.toString().equals(uri.toString())) {
                    return file;
                }
            }
        }
        return null;
    }
    */
    fun findUriInScope(context: Context?, scope: Uri, uri: Uri): DocumentFile? {
        var treeUri = DocumentFile.fromTreeUri(context!!, scope)
        val trailScope = getTrailFromUri(scope)
        val trailVideo = getTrailFromUri(uri)
        for (i in trailVideo.indices) {
            if (i < trailScope.size) {
                if (trailScope[i] != trailVideo[i]) break
            } else {
                treeUri = treeUri!!.findFile(trailVideo[i])
                if (treeUri == null) break
            }
            if (i + 1 == trailVideo.size) return treeUri
        }
        return null
    }

    fun findDocInScope(scope: DocumentFile?, doc: DocumentFile?): DocumentFile? {
        if (doc == null || scope == null) return null
        for (file in scope.listFiles()) {
            if (file.isDirectory) {
                val ret = findDocInScope(file, doc)
                if (ret != null) return ret
            } else {
                //if (doc.length() == file.length() && doc.lastModified() == file.lastModified() && doc.getName().equals(file.getName())) {
                // lastModified is zero when opened from Solid Explorer
                val docName = doc.name
                val fileName = file.name
                if (docName == null || fileName == null) {
                    continue
                }
                if (doc.length() == file.length() && docName == fileName) {
                    return file
                }
            }
        }
        return null
    }

    fun getTrailPathFromUri(uri: Uri): String {
        val path = uri.path
        val array = path!!.split(":").toTypedArray()
        return if (array.size > 1) {
            array[array.size - 1]
        } else {
            path
        }
    }

    fun getTrailFromUri(uri: Uri): Array<String> {
        if ("org.courville.nova.provider" == uri.host && ContentResolver.SCHEME_CONTENT == uri.scheme) {
            val path = uri.path
            if (path!!.startsWith("/external_files/")) {
                return path.substring("/external_files/".length).split("/").toTypedArray()
            }
        }
        return getTrailPathFromUri(uri).split("/").toTypedArray()
    }

    private fun getFileBaseName(name: String?): String {
        return if (name!!.indexOf(".") > 0) name.substring(0, name.lastIndexOf(".")) else name
    }

    fun findSubtitle(video: DocumentFile): DocumentFile? {
        val dir = video.parentFile
        return findSubtitle(video, dir)
    }

    fun findSubtitle(video: DocumentFile, dir: DocumentFile?): DocumentFile? {
        val videoName = getFileBaseName(video.name)
        var videoFiles = 0
        if (dir == null || !dir.isDirectory) return null
        val candidates: MutableList<DocumentFile> = ArrayList()
        for (file in dir.listFiles()) {
            if (file.name!!.startsWith(".")) continue
            if (isSubtitleFile(file)) candidates.add(file)
            if (isVideoFile(file)) videoFiles++
        }
        if (videoFiles == 1 && candidates.size == 1) {
            return candidates[0]
        }
        if (candidates.size >= 1) {
            for (candidate in candidates) {
                if (candidate.name!!.startsWith("$videoName.")) {
                    return candidate
                }
            }
        }
        return null
    }

    fun findNext(video: DocumentFile): DocumentFile? {
        val dir = video.parentFile
        return findNext(video, dir)
    }

    fun findNext(video: DocumentFile, dir: DocumentFile?): DocumentFile? {
        val list = dir!!.listFiles()
        try {
            Arrays.sort(list) { a: DocumentFile, b: DocumentFile ->
                a.name!!
                    .compareTo(b.name!!, ignoreCase = true)
            }
        } catch (e: NullPointerException) {
            return null
        }
        val videoName = video.name
        var matchFound = false
        for (file in list) {
            if (file.name == videoName) {
                matchFound = true
            } else if (matchFound) {
                if (isVideoFile(file)) {
                    return file
                }
            }
        }
        return null
    }

    fun isVideoFile(file: DocumentFile): Boolean {
        return file.isFile && file.type!!.startsWith("video/")
    }

    fun isSubtitleFile(file: DocumentFile): Boolean {
        if (!file.isFile) return false
        val name = file.name!!.lowercase(Locale.getDefault())
        return (name.endsWith(".srt") || name.endsWith(".ssa") || name.endsWith(".ass")
                || name.endsWith(".vtt") || name.endsWith(".ttml"))
    }

    fun isSubtitle(uri: Uri?, mimeType: String?): Boolean {
        if (mimeType != null) {
            for (mime in Utils.supportedMimeTypesSubtitle) {
                if (mimeType == mime) {
                    return true
                }
            }
            if (mimeType == "text/plain" || mimeType == "text/x-ssa" || mimeType == "application/octet-stream" || mimeType == "application/ass" || mimeType == "application/ssa" || mimeType == "application/vtt") {
                return true
            }
        }
        if (uri != null) {
            if (Utils.isSupportedNetworkUri(uri)) {
                var path = uri.path
                if (path != null) {
                    path = path.lowercase(Locale.getDefault())
                    for (extension in Utils.supportedExtensionsSubtitle) {
                        if (path.endsWith(".$extension")) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun clearCache(context: Context) {
        try {
            if (context.cacheDir.listFiles() == null) return
            for (file in context.cacheDir.listFiles()!!) {
                if (file.isFile) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun convertToUTF(activity: VideoPlayerActivity, subtitleUri: Uri): Uri? {
        try {
            val scheme = subtitleUri.scheme
            return if (scheme != null && scheme.lowercase(Locale.getDefault()).startsWith("http")) {
                val urls: MutableList<Uri> = ArrayList()
                urls.add(subtitleUri)
                val subtitleFetcher = SubtitleFetcher(activity, urls)
                subtitleFetcher.start()
                null
            } else {
                val inputStream = activity.contentResolver.openInputStream(subtitleUri)
                convertInputStreamToUTF(activity, subtitleUri, inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return subtitleUri
    }

    fun convertInputStreamToUTF(
        context: Context,
        subtitleUri: Uri?,
        inputStream: InputStream?
    ): Uri? {
        var subtitleUri = subtitleUri
        try {
            val detector = CharsetDetector()
            val bufferedInputStream = BufferedInputStream(inputStream)
            detector.setText(bufferedInputStream)
            val charsetMatch = detector.detect()
            if (StandardCharsets.UTF_8.displayName() != charsetMatch.name) {
                var filename = subtitleUri!!.path
                filename = filename!!.substring(filename.lastIndexOf("/") + 1)
                val file = File(context.cacheDir, filename)
                val bufferedReader = BufferedReader(charsetMatch.reader)
                val bufferedWriter = BufferedWriter(FileWriter(file))
                val buffer = CharArray(512)
                var num: Int
                var pass = 0
                var success = true
                while (bufferedReader.read(buffer).also { num = it } != -1) {
                    bufferedWriter.write(buffer, 0, num)
                    pass++
                    if (pass * 512 > 2000000) {
                        success = false
                        break
                    }
                }
                bufferedWriter.close()
                bufferedReader.close()
                subtitleUri = if (success) {
                    Uri.fromFile(file)
                } else {
                    null
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return subtitleUri
    }

    fun buildSubtitle(
        context: Context,
        uri: Uri,
        subtitleName: String?,
        selected: Boolean
    ): SubtitleConfiguration {
        var subtitleName = subtitleName
        val subtitleMime = getSubtitleMime(uri)
        val subtitleLanguage = getSubtitleLanguage(uri)
        if (subtitleLanguage == null && subtitleName == null) subtitleName =
            Utils.getFileName(context, uri)
        val subtitleConfigurationBuilder = SubtitleConfiguration.Builder(uri)
            .setMimeType(subtitleMime)
            .setLanguage(subtitleLanguage)
            .setRoleFlags(C.ROLE_FLAG_SUBTITLE)
            .setLabel(subtitleName)
        if (selected) {
            subtitleConfigurationBuilder.setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
        }
        return subtitleConfigurationBuilder.build()
    }

    fun normalizeFontScale(fontScale: Float, small: Boolean): Float {
        // https://bbc.github.io/subtitle-guidelines/#Presentation-font-size
        // ¯\_(ツ)_/¯
        val newScale: Float = if (fontScale > 1.01f) {
            if (fontScale >= 1.99f) {
                // 2.0
                if (small) 1.15f else 1.2f
            } else {
                // 1.5
                if (small) 1.0f else 1.1f
            }
        } else if (fontScale < 0.99f) {
            if (fontScale <= 0.26f) {
                // 0.25
                if (small) 0.65f else 0.8f
            } else {
                // 0.5
                if (small) 0.75f else 0.9f
            }
        } else {
            if (small) 0.85f else 1.0f
        }
        return newScale
    }
}