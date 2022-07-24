package com.ntduc.utils

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ntduc.utils.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnActivityUtils.setOnClickListener {
            startActivity(Intent(this, ActiUtilsActivity::class.java))
        }

        binding.btnAnimationUtils.setOnClickListener {
            startActivity(Intent(this, AnimUtilsActivity::class.java))
        }
    }
}