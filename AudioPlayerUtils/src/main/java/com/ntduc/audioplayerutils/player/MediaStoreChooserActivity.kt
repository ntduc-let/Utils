package com.ntduc.audioplayerutils.player

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.ntduc.audioplayerutils.R
import java.util.*
import java.util.Collections.sort

class MediaStoreChooserActivity : Activity() {
    val REQUEST_PERMISSION_STORAGE = 0
    var bucketId: Int? = null
    var subtitles = false
    var title: String? = null
    @RequiresApi(api = Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        if (intent.hasExtra(BUCKET_ID)) {
            bucketId = intent.getIntExtra(BUCKET_ID, Int.MIN_VALUE)
        }
        subtitles = intent.getBooleanExtra(SUBTITLES, false)
        title = intent.getStringExtra(TITLE)
        var permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (Build.VERSION.SDK_INT >= 33 && applicationContext.applicationInfo.targetSdkVersion >= 33) {
            permission = Manifest.permission.READ_MEDIA_VIDEO
        }
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            start()
        } else {
            requestPermissions(arrayOf(permission), REQUEST_PERMISSION_STORAGE)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            setResult(RESULT_OK, data)
            finish()
        } else {
            start()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private fun start() {
        if (bucketId == null) {
            showBuckets()
        } else {
            showFiles(bucketId!!)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_STORAGE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                start()
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun query(
        projectionId: String,
        projectionName: String,
        selection: String?
    ): HashMap<Int, String> {
        var collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        if (subtitles) {
            collection = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
        }
        val hashMap = HashMap<Int, String>()
        contentResolver.query(
            collection!!,
            arrayOf(projectionId, projectionName),
            selection,
            null,
            null
        ).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val columnId = cursor.getColumnIndex(projectionId)
                val columnName = cursor.getColumnIndex(projectionName)
                do {
                    val id = cursor.getInt(columnId)
                    val name = cursor.getString(columnName) ?: continue
                    if (!hashMap.containsKey(id)) {
                        hashMap[id] = name
                    }
                } while (cursor.moveToNext())
            }
        }
        // Sort map by value
        val list: List<Map.Entry<Int, String>> = LinkedList<Map.Entry<Int, String>>(hashMap.entries)
        sort(list) { (_, value): Map.Entry<Int, String>, (_, value1): Map.Entry<Int, String> ->
            value.compareTo(
                value1, ignoreCase = true
            )
        }
        val sortedMap: HashMap<Int, String> = LinkedHashMap()
        for ((key, value) in list) {
            sortedMap[key] = value
        }
        return sortedMap
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    fun showBuckets() {
        var selection = ""
        if (subtitles) {
            selection += MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_SUBTITLE
        }
        val buckets = query(
            MediaStore.MediaColumns.BUCKET_ID,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
            selection
        )
        val bucketIds = buckets.keys.toTypedArray()
        val bucketDisplayNames = buckets.values.toTypedArray()
        val alertDialogBuilder: AlertDialog.Builder
        if (buckets.size == 0) {
            alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage(R.string.mediastore_empty)
        } else {
            alertDialogBuilder = AlertDialog.Builder(this, R.style.MediaStoreChooserDialog)
            alertDialogBuilder.setTitle(getString(R.string.choose_file))
            alertDialogBuilder.setItems(bucketDisplayNames) { _: DialogInterface?, i: Int ->
                val intent =
                    Intent(this@MediaStoreChooserActivity, MediaStoreChooserActivity::class.java)
                intent.putExtra(SUBTITLES, subtitles)
                intent.putExtra(BUCKET_ID, bucketIds[i])
                intent.putExtra(TITLE, bucketDisplayNames[i])
                startActivityForResult(intent, 0)
            }
        }
        alertDialogBuilder.setOnCancelListener { finish() }
        alertDialogBuilder.show()
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    fun showFiles(bucketId: Int) {
        var selection = MediaStore.MediaColumns.BUCKET_ID + "=" + bucketId
        if (subtitles) {
            selection += " AND " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_SUBTITLE
        }
        val files =
            query(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME, selection)
        val ids = files.keys.toTypedArray()
        val displayNames = files.values.toTypedArray()
        val alertDialogBuilder = AlertDialog.Builder(this, R.style.MediaStoreChooserDialog)
        if (title != null) {
            alertDialogBuilder.setTitle(title)
        }
        alertDialogBuilder.setItems(displayNames) { _: DialogInterface?, i: Int ->
            val contentUri: Uri = if (subtitles) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL, ids[i].toLong())
            } else {
                ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    ids[i].toLong()
                )
            }
            val data = Intent("RESULT", contentUri)
            setResult(RESULT_OK, data)
            finish()
        }
        alertDialogBuilder.setOnCancelListener { finish() }
        alertDialogBuilder.show()
    }

    companion object {
        const val BUCKET_ID = "BUCKET_ID"
        const val SUBTITLES = "SUBTITLES"
        const val TITLE = "TITLE"
    }
}