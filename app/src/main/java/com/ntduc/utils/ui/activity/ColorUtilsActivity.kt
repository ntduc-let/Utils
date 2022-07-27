package com.ntduc.utils.ui.activity

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ntduc.colorutils.*
import com.ntduc.utils.databinding.ActivityColorUtilsBinding

class ColorUtilsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityColorUtilsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRandomColor.setOnClickListener {
            binding.btnRandomColor.setBackgroundColor(randomColor)
        }

        binding.btnGetDisabledColor.setOnClickListener {
            binding.btnGetDisabledColor.setBackgroundColor(getDisabledColor())
        }

        binding.btnSetColorAlpha.setOnClickListener {
            binding.btnSetColorAlpha.setBackgroundColor(setColorAlpha(Color.RED, 0.5f))
        }

        binding.edtTint.tint(Color.RED)
        binding.txtTitle.setTextColor(getTitleTextColor(Color.YELLOW))
        binding.txtBody.setTextColor(getBodyTextColor(Color.YELLOW))
    }
}