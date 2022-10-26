package com.ntduc.videoplayerutils.file_chooser.internals

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.ListView
import java.util.*

object UiUtil {
    fun dip2px(dipValue: Int): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return java.lang.Float.valueOf(dipValue * scale + 0.5f).toInt()
    }

    fun dip2px(dipValue: Float): Float {
        val scale = Resources.getSystem().displayMetrics.density
        return dipValue * scale + 0.5f
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun resolveFileTypeIcon(ctx: Context, fileUri: Uri): Drawable? {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(fileUri, getMimeType(ctx, fileUri))
        val pm = ctx.packageManager
        val matches = pm.queryIntentActivities(intent, 0)
        for (match in matches) {
            //final CharSequence label = match.loadLabel(pm);
            return match.loadIcon(pm)
        }
        return null //ContextCompat.getDrawable(ctx, R.drawable.ic_file);
    }

    private fun getMimeType(ctx: Context, uri: Uri): String? {
        val mimeType: String? = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            val cr = ctx.applicationContext.contentResolver
            cr.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase(Locale.getDefault())
            )
        }
        return mimeType
    }

    // This only works assuming that all list items have the same height!
    fun getListYScroll(list: ListView): Int {
        val child = list.getChildAt(0)
        return if (child == null) -1 else list.firstVisiblePosition * child.height - child.top + list.paddingTop
    }

    fun hideKeyboardFrom(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}