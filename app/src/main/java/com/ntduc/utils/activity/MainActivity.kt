package com.ntduc.utils.activity

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

        binding.btnColorUtils.setOnClickListener {
            startActivity(Intent(this, ColorUtilsActivity::class.java))
        }

        binding.btnContextUtils.setOnClickListener {
            startActivity(Intent(this, ContextUtilsActivity::class.java))
        }

        binding.btnViewPager2Utils.setOnClickListener {
            startActivity(Intent(this, ViewPager2UtilsActivity::class.java))
        }
    }
}