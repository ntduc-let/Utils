package com.ntduc.utils.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ntduc.contextutils.inflater
import com.ntduc.utils.R
import com.ntduc.utils.databinding.ActivityZipBinding

class ZipActivity : AppCompatActivity() {
    private lateinit var binding: ActivityZipBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityZipBinding.inflate(inflater)
        setContentView(binding.root)
    }
}