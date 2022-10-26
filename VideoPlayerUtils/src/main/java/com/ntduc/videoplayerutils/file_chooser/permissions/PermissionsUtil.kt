package com.ntduc.videoplayerutils.file_chooser.permissions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.SparseArray
import java.util.*

object PermissionsUtil {

    fun checkPermissions(
        context: Context,
        onPermissionListener: OnPermissionListener?, vararg permissions: String?
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissions.isEmpty()) {
            onPermissionListener?.onPermissionGranted(permissions)
            return
        }
        val requestCode = _random.nextInt(1024)
        _permissionListeners.put(requestCode, onPermissionListener)
        context.startActivity(
            Intent(context, PermissionActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .putExtra(PermissionActivity.INTENT_EXTRA_PERMISSIONS, permissions)
                .putExtra(PermissionActivity.INTENT_EXTRA_REQUEST_CODE, requestCode)
        )
    }

    private val _permissionListeners = SparseArray<OnPermissionListener?>()
    private val _random = Random()

    fun getPermissionListener(requestCode: Int): OnPermissionListener? {
        val listener = _permissionListeners[requestCode, null]
        _permissionListeners.remove(requestCode)
        return listener
    }

    interface OnPermissionListener {
        fun onPermissionGranted(permissions: Array<out String?>?)
        fun onPermissionDenied(permissions: Array<String?>?)
        fun onShouldShowRequestPermissionRationale(permissions: Array<String?>?)
    }
}