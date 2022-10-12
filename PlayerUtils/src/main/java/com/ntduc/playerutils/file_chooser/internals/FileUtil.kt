package com.ntduc.playerutils.file_chooser.internals

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import java.io.IOException
import java.lang.NullPointerException
import java.lang.reflect.Array
import java.lang.reflect.InvocationTargetException
import java.text.DecimalFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.Throws

/**
 * Created by coco on 6/7/15.
 */
object FileUtil {

    //Kích thước File
    fun getReadableFileSize(size: Long): String {
        var fileSize: Float
        var suffix = Constants.KILOBYTES
        if (size < Constants.BYTES_IN_KILOBYTES) {
            fileSize = size.toFloat()
            suffix = Constants.BYTES
        } else {
            fileSize = size.toFloat() / Constants.BYTES_IN_KILOBYTES
            if (fileSize >= Constants.BYTES_IN_KILOBYTES) {
                fileSize /= Constants.BYTES_IN_KILOBYTES
                if (fileSize >= Constants.BYTES_IN_KILOBYTES) {
                    fileSize /= Constants.BYTES_IN_KILOBYTES
                    suffix = Constants.GIGABYTES
                } else {
                    suffix = Constants.MEGABYTES
                }
            }
        }
        return DecimalFormat("###.#").format(fileSize.toDouble()) + suffix
    }

    //Đường dẫn file
    fun getStoragePath(context: Context, isRemovable: Boolean): String? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            getStoragePathLow(context, isRemovable)
        } else {
            getStoragePath24(context, isRemovable)
        }
    }

    //?
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun getStoragePath24(context: Context, isRemovable: Boolean): String? {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        try {
            val result = Objects.requireNonNull(storageManager).storageVolumes
            for (vol in result) {
                Log.d("X", "  ---Object--" + vol + " | desc: " + vol.getDescription(context))
                if (isRemovable != vol.isRemovable) {
                    continue
                }
                return if (Build.VERSION.SDK_INT >= 30) {
                    // TODO: Handle multiple removable volumes
                    val dir = vol.directory ?: continue
                    dir.absolutePath
                } else {
                    val getPath = vol.javaClass.getMethod("getPath")
                    val path = getPath.invoke(vol) as String
                    Log.d("X", "    ---path--$path")
                    // HACK
                    if (isRemovable && result.size > 2 && path.startsWith("/storage/")) "/storage" else path
                }
            }
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return Environment.getExternalStorageDirectory().absolutePath
    }

    //?
    private fun getStoragePathLow(context: Context, isRemovable: Boolean): String? {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val storageVolumeClazz: Class<*>?
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList = storageManager.javaClass.getMethod("getVolumeList")
            val getPath = storageVolumeClazz.getMethod("getPath")
            val isRemovableMtd = storageVolumeClazz.getMethod("isRemovable")
            val result = getVolumeList.invoke(storageManager) ?: return null
            val length = Array.getLength(result)
            //final int length = result.size();
            Log.d("X", "---length--$length")
            for (i in 0 until length) {
                val storageVolumeElement = Array.get(result, i)
                Log.d("X", "  ---Object--" + storageVolumeElement + "i==" + i)
                val path = getPath.invoke(storageVolumeElement) as String
                Log.d("X", "  ---path_total--$path")
                val removable = isRemovableMtd.invoke(storageVolumeElement) as Boolean
                if (isRemovable == removable) {
                    Log.d("X", "    ---path--$path")
                    // HACK
                    return if (isRemovable && length > 2 && path.startsWith("/storage/")) "/storage" else path
                }
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return Environment.getExternalStorageDirectory().absolutePath
    }

    //Đường dẫn file
    fun getStoragePaths(context: Context): LinkedHashMap<String, String>? {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            getStoragePathsLow(context)
        } else {
            getStoragePaths24(context)
        }
    }

    //?
    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun getStoragePaths24(context: Context): LinkedHashMap<String, String> {
        val paths = LinkedHashMap<String, String>()
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        try {
            val result = Objects.requireNonNull(storageManager).storageVolumes
            for (vol in result) {
                Log.d("X", "  ---Object--" + vol + " | desc: " + vol.getDescription(context))
                if (Build.VERSION.SDK_INT >= 30) {
                    val dir = vol.directory ?: continue
                    paths[dir.absolutePath] = formatPathAsLabel(dir.absolutePath)
                } else {
                    val getPath = vol.javaClass.getMethod("getPath")
                    val path = getPath.invoke(vol) as String
                    Log.d("X", "    ---path--$path")
                    paths[path] = formatPathAsLabel(path)
                }
            }
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        if (paths.size == 0) {
            val path = Environment.getExternalStorageDirectory().absolutePath
            paths[path] = formatPathAsLabel(path)
        }
        return paths
    }

    //?
    private fun getStoragePathsLow(context: Context): LinkedHashMap<String, String>? {
        val paths = LinkedHashMap<String, String>()
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val storageVolumeClazz: Class<*>?
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList = storageManager.javaClass.getMethod("getVolumeList")
            val getPath = storageVolumeClazz.getMethod("getPath")
            val isRemovableMtd = storageVolumeClazz.getMethod("isRemovable")
            val result = getVolumeList.invoke(storageManager) ?: return null
            val length = Array.getLength(result)
            Log.d("X", "---length--$length")
            for (i in 0 until length) {
                val storageVolumeElement = Array.get(result, i)
                Log.d("X", "  ---Object--" + storageVolumeElement + "i==" + i)
                val path = getPath.invoke(storageVolumeElement) as String
                Log.d("X", "  ---path_total--$path")
                isRemovableMtd.invoke(storageVolumeElement) as Boolean
                Log.d("X", "    ---path--$path")
                paths[path] = formatPathAsLabel(path)
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        if (paths.size == 0) {
            val path = Environment.getExternalStorageDirectory().absolutePath
            paths[path] = formatPathAsLabel(path)
        }
        return paths
    }

    //?
    private fun formatPathAsLabel(path: String): String {
        return "[ $path ]"
    }

    //Xóa file (đệ quy)
    @Throws(IOException::class)
    fun deleteFileRecursively(file: File) {
        if (file.isDirectory) {
            val entries = file.listFiles() ?: return
            for (entry in entries) {
                deleteFileRecursively(entry)
            }
        }
        if (!file.delete()) {
            throw IOException("Couldn't delete \"" + file.name + "\" at \"" + file.parent)
        }
    }

    //Folder hiện tại
    private val currentDirectory: File
        get() = File(File("").absolutePath)

    //Tạo Folder mới
    fun createNewDirectory(name: String, parent: File? = currentDirectory): Boolean {
        val newDir = File(parent, name)
        return !newDir.exists() && newDir.mkdir()
    }

    private object Constants {
        const val BYTES_IN_KILOBYTES = 1024
        const val BYTES = " B"
        const val KILOBYTES = " KB"
        const val MEGABYTES = " MB"
        const val GIGABYTES = " GB"
    }

    class NewFolderFilter constructor(
        private val maxLength: Int = 255,
        pattern: String = "^[^/<>|\\\\:&;#\n\r\t?*~\u0000-\u001f]*$"
    ) : InputFilter {
        private val pattern: Pattern

        /**
         * examples:
         * a simple allow only regex pattern: "^[a-z0-9]*$" (only lower case letters and numbers)
         * a simple anything but regex pattern: "^[^0-9;#&amp;]*$" (ban numbers and '&amp;', ';', '#' characters)
         */
        init {
            this.pattern = Pattern.compile(pattern)
        }

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val matcher = pattern.matcher(source)
            if (!matcher.matches()) {
                return if (source is SpannableStringBuilder) dest.subSequence(dstart, dend) else ""
            }
            var keep = maxLength - (dest.length - (dend - dstart))
            return if (keep <= 0) {
                ""
            } else if (keep >= end - start) {
                null // keep original
            } else {
                keep += start
                if (Character.isHighSurrogate(source[keep - 1])) {
                    --keep
                    if (keep == start) {
                        return ""
                    }
                }
                source.subSequence(start, keep).toString()
            }
        }
    }
}