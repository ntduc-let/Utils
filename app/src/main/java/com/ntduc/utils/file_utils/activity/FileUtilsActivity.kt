package com.ntduc.utils.file_utils.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ntduc.toastutils.shortToast
import com.ntduc.utils.databinding.ActivityFileUtilsBinding
import com.ntduc.fileutils.*
import com.ntduc.stringutils.asFile
import com.ntduc.utils.file_utils.get_all_file.activity.GetAllFileActivity
import com.ntduc.utils.file_utils.get_all_image.activity.GetAllImageActivity


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
    private val selectFolderCopyFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val intent = it.data ?: return@registerForActivityResult
                val docUri = DocumentsContract.buildDocumentUriUsingTree(
                    intent.data,
                    DocumentsContract.getTreeDocumentId(intent.data)
                )
                val uri = docUri.getRealPath(this)
                if (uri != null) {
                    if (copyFile(
                            file = binding.txt.text.toString().asFile(),
                            dest = uri.asFile(),
                            overwrite = true,
                            onCompleted = {
                                Log.d("aaaaaaaaaaaaaaaa", "onCompleted")
                            })
                    ) {
                        shortToast("Copy Success")
                    } else {
                        shortToast("Copy Error")
                    }
                }
            } else {
                shortToast("Cancel")
            }
        }
    private val selectFolderMoveFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val intent = it.data ?: return@registerForActivityResult
                val docUri = DocumentsContract.buildDocumentUriUsingTree(
                    intent.data,
                    DocumentsContract.getTreeDocumentId(intent.data)
                )
                val uri = docUri.getRealPath(this)
                if (uri != null) {
                    if (moveFile(
                            file = binding.txt.text.toString().asFile(),
                            dest = uri.asFile(),
                            overwrite = true,
                            onCompleted = {
                                Log.d("aaaaaaaaaaaaaaaa", "onCompleted")
                            })
                    ) {
                        shortToast("Move Success")
                    } else {
                        shortToast("Move Error")
                    }
                }
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

        binding.btnCopyFile.setOnClickListener {
            if (binding.txt.text.trim().isEmpty()) {
                shortToast("Please select file")
                return@setOnClickListener
            }
            selectFolderCopyFile()
        }

        binding.btnMoveFile.setOnClickListener {
            if (binding.txt.text.trim().isEmpty()) {
                shortToast("Please select file")
                return@setOnClickListener
            }
            selectFolderMoveFile()
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

        binding.btnZip.setOnClickListener {
            startActivity(Intent(this, ZipActivity::class.java))
        }
    }

    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val uri = Uri.parse(Environment.getDownloadCacheDirectory().path)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setDataAndType(uri, "*/*")
        selectFileLauncher.launch(intent)
    }

    private fun selectFolderCopyFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        selectFolderCopyFileLauncher.launch(intent)
    }

    private fun selectFolderMoveFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        selectFolderMoveFileLauncher.launch(intent)
    }
}