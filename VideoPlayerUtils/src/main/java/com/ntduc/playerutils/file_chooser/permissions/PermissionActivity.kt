package com.ntduc.playerutils.file_chooser.permissions

import android.content.pm.PackageManager
import android.os.Bundle
import com.ntduc.playerutils.file_chooser.permissions.PermissionsUtil.getPermissionListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import com.ntduc.playerutils.file_chooser.permissions.PermissionsUtil.OnPermissionListener
import java.lang.RuntimeException
import java.util.ArrayList

class PermissionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val permissions = intent.getStringArrayExtra(INTENT_EXTRA_PERMISSIONS)
        if (permissions!!.isEmpty()) finish()
        _requestCode = intent.getIntExtra(INTENT_EXTRA_REQUEST_CODE, -1)
        if (_requestCode == -1) finish()
        _permissionListener = getPermissionListener(_requestCode)
        for (permission in permissions) {
            if (permission == null || permission.isEmpty()) {
                throw RuntimeException("permission can't be null or empty")
            }
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                _permissions_granted.add(permission)
            } else {
                _permissions_denied.add(permission)
            }
        }
        if (_permissions_denied.isEmpty()) {
            if (_permissions_granted.isEmpty()) {
                throw RuntimeException("there are no permissions")
            } else {
                if (_permissionListener != null) {
                    _permissionListener!!.onPermissionGranted(_permissions_granted.toTypedArray())
                }
                finish()
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                _permissions_denied.toTypedArray(),
                _requestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != _requestCode) {
            finish()
        }
        _permissions_denied.clear()
        for (i in permissions.indices.reversed()) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                _permissions_granted.add(permissions[i])
            } else {
                _permissions_denied.add(permissions[i])
            }
        }
        if (_permissions_denied.isEmpty()) {
            if (_permissions_granted.isEmpty()) {
                // HACK: https://github.com/hedzr/android-file-chooser/issues/73
                //throw new RuntimeException("there are no permissions");
                finish()
            } else {
                if (_permissionListener != null) {
                    _permissionListener!!.onPermissionGranted(_permissions_granted.toTypedArray())
                }
                finish()
            }
        } else {
            val permissionsShouldRequest: MutableList<String?> = ArrayList()
            for (permission in _permissions_denied) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission!!)) {
                    permissionsShouldRequest.add(permission)
                }
            }
            if (_permissionListener != null) {
                _permissionListener!!.onPermissionDenied(_permissions_denied.toTypedArray())
                _permissionListener!!.onShouldShowRequestPermissionRationale(
                    permissionsShouldRequest.toTypedArray()
                )
            }
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            overridePendingTransition(0, 0)
        }
    }

    private var _permissionListener: OnPermissionListener? = null
    var _requestCode = 0
    private val _permissions_granted: MutableList<String?> = ArrayList()
    private val _permissions_denied: MutableList<String?> = ArrayList()

    companion object {
        private val TAG = PermissionActivity::class.java.name
        const val INTENT_EXTRA_PERMISSIONS = "PERMISSIONS"
        const val INTENT_EXTRA_REQUEST_CODE = "REQUEST_CODE"
    }
}