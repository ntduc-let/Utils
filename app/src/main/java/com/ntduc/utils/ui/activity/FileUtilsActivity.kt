package com.ntduc.utils.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ntduc.contextutils.showConfirmationDialog
import com.ntduc.toastutils.shortToast
import com.ntduc.utils.BuildConfig
import com.ntduc.utils.databinding.ActivityFileUtilsBinding
import com.prox.fileutils.deleteFiles
import com.prox.fileutils.getRealPath
import com.prox.fileutils.renameFile
import com.prox.fileutils.shareFile
import com.prox.stringutils.asFile

class FileUtilsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFileUtilsBinding

    companion object {
        private const val REQUEST_PERMISSION_READ_WRITE = 100
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                if (checkPermissionReadAllFile()) {
                    selectFile()
                } else {
                    requestPermissionReadAllFile()
                }
            }
        }

    private val selectFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val intent = it.data ?: return@registerForActivityResult
                binding.txt.text = intent.data?.getRealPath(this) ?: "not found"
            } else {
                shortToast("Cancel")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileUtilsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectFile.setOnClickListener {
            if (checkPermissionReadAllFile()) {
                selectFile()
            } else {
                requestPermissionReadAllFile()
            }
        }

        binding.btnRenameFile.setOnClickListener {
            if (binding.txt.text.trim().isEmpty()) {
                shortToast("Please select file")
                return@setOnClickListener
            }

            if (binding.edtRenameFile.text.trim().isEmpty()) {
                shortToast("Please enter name file")
                return@setOnClickListener
            }
            if (renameFile(binding.txt.text.toString().asFile(), binding.edtRenameFile.text.toString(), onCompleted = {
                    Log.d("aaaaaaaaaaaaaaaa", "onCompleted")
                })) {
                shortToast("Rename success")
                binding.txt.text = ""
                binding.edtRenameFile.setText("")
            } else {
                shortToast("Rename false")
            }
        }

        binding.btnDeleteFile.setOnClickListener {
            if (binding.txt.text.trim().isEmpty()) {
                shortToast("Please select file")
                return@setOnClickListener
            }

            if (deleteFiles(listOf(binding.txt.text.toString().asFile()), onCompleted = {
                    Log.d("aaaaaaaaaaaaaaaa", "onCompleted")
                })) {
                shortToast("Delete success")
                binding.txt.text = ""
            } else {
                shortToast("Delete false")
            }
        }

        binding.btnShareFile.setOnClickListener {
            if (binding.txt.text.trim().isEmpty()) {
                shortToast("Please select file")
                return@setOnClickListener
            }

            shareFile(binding.txt.text.toString().asFile(), "com.ntduc.utils.provider")
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val uri = Uri.parse(Environment.getDownloadCacheDirectory().path)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setDataAndType(uri, "*/*")
        selectFileLauncher.launch(intent)
    }

    private fun checkPermissionReadAllFile(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val write =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissionReadAllFile() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            openDialogAccessAllFile()
        } else {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            requestPermissions(permissions, REQUEST_PERMISSION_READ_WRITE)
        }
    }

    private fun requestAccessAllFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri)
                requestPermissionLauncher.launch(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                requestPermissionLauncher.launch(intent)
            }
        } else {
            try {
                val uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)
                requestPermissionLauncher.launch(intent)
            } catch (e: Exception) {
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_READ_WRITE) {
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                selectFile()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(permissions[0]!!)
                        && shouldShowRequestPermissionRationale(permissions[1]!!)
                    ) {
                        requestPermissions(permissions, REQUEST_PERMISSION_READ_WRITE)
                    } else {
                        openDialogAccessAllFile()
                    }
                }
            }
        }
    }

    private fun openDialogAccessAllFile() {
        showConfirmationDialog(
            "Request Permission",
            "Access to read all file in your device",
            onResponse = {
                when (it) {
                    true -> requestAccessAllFile()
                    false -> finish()
                }
            })
    }
}