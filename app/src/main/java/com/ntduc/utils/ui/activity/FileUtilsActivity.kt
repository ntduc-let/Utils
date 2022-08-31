package com.ntduc.utils.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ntduc.toastutils.shortToast
import com.ntduc.utils.databinding.ActivityFileUtilsBinding
import com.prox.fileutils.deleteFiles
import com.prox.fileutils.getRealPath
import com.prox.fileutils.renameFile
import com.prox.fileutils.shareFile
import com.prox.stringutils.asFile

class FileUtilsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFileUtilsBinding

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
            selectFile()
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
            if (renameFile(
                    binding.txt.text.toString().asFile(),
                    binding.edtRenameFile.text.toString(),
                    onCompleted = {
                        Log.d("aaaaaaaaaaaaaaaa", "onCompleted")
                    })
            ) {
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

        binding.btnGetAllFile.setOnClickListener {
            startActivity(Intent(this, GetAllFileActivity::class.java))
        }

        binding.btnGetAllAudio.setOnClickListener {
            startActivity(Intent(this, GetAllAudioActivity::class.java))
        }

        binding.btnGetAllImage.setOnClickListener {
            startActivity(Intent(this, GetAllImageActivity::class.java))
        }

        binding.btnGetAllVideo.setOnClickListener {
            startActivity(Intent(this, GetAllVideoActivity::class.java))
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val uri = Uri.parse(Environment.getDownloadCacheDirectory().path)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setDataAndType(uri, "*/*")
        selectFileLauncher.launch(intent)
    }
}