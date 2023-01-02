package com.ntduc.utils.security_utils

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.ntduc.contextutils.inflater
import com.ntduc.securityutils.AndroidEncryption
import com.ntduc.utils.databinding.ActivitySecurityUtilsBinding
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@RequiresApi(Build.VERSION_CODES.M)
class SecurityUtilsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecurityUtilsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityUtilsBinding.inflate(inflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        initEvent()
    }

    private fun initEvent() {
        binding.btnGenerateKey.setOnClickListener {
            AndroidEncryption.generateKey(this)
        }

        binding.btnEncrypt.setOnClickListener {
            binding.txt.text = AndroidEncryption.encrypt(binding.edt.text.toString())
//            val s = binding.edt.text.toString()
//            val file = File(filesDir, "secret.txt")
//            if (!file.exists()){
//                file.createNewFile()
//            }
//            val fos = FileOutputStream(file)
//            binding.txt.text = AndroidEncryption.encrypt(s, fos)
        }

        binding.btnDecrypt.setOnClickListener {
            binding.edt.setText(AndroidEncryption.decrypt(binding.txt.text.toString()))
//            val file = File(filesDir, "secret.txt")
//            binding.edt.setText("${AndroidEncryption.decrypt(FileInputStream(file))}")
        }
    }
}