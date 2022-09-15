package com.ntduc.utils.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.ntduc.contextutils.inflater
import com.ntduc.toastutils.shortToast
import com.ntduc.utils.databinding.ActivityZipBinding
import com.prox.fileutils.copyFile
import com.prox.fileutils.getRealPath
import com.prox.stringutils.asFile

class ZipActivity : AppCompatActivity() {
    private lateinit var binding: ActivityZipBinding

    private val selectFolderLauncher =
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

    @SuppressLint("SetTextI18n")
    private val selectFilesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val intent = it.data ?: return@registerForActivityResult
                if (intent.clipData == null){
                    binding.txt.text = intent.data?.getRealPath(this) ?: "not found"
                }else{
                    if (intent.clipData!!.itemCount==0){
                        binding.txt.text = "not found"
                    }else{
                        binding.txt.text = ""
                        for (index in 0 until intent.clipData!!.itemCount){
                            val uri = intent.clipData!!.getItemAt(index).uri
                            binding.txt.text = "${binding.txt.text}${uri?.getRealPath(this)}\n"
                        }
                    }
                }
            } else {
                shortToast("Cancel")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityZipBinding.inflate(inflater)
        setContentView(binding.root)

        binding.btnSelectFiles.setOnClickListener {
            selectFiles()
        }
    }

    private fun selectFiles() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        val uri = Uri.parse(Environment.getDownloadCacheDirectory().path)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setDataAndType(uri, "*/*")
        selectFilesLauncher.launch(intent)
    }

    private fun selectFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        selectFolderLauncher.launch(intent)
    }
}