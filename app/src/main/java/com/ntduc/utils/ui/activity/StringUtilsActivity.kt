package com.ntduc.utils.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ntduc.colorutils.randomColor
import com.ntduc.toastutils.shortToast
import com.ntduc.utils.databinding.ActivityStringUtilsBinding
import com.prox.stringutils.*

class StringUtilsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityStringUtilsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnIsNumeric.setOnClickListener {
            shortToast("${binding.edt.text.toString().isNumeric}")
        }

        binding.btnConvertToCamelCase.setOnClickListener {
            shortToast(binding.edt.text.toString().convertToCamelCase())
        }

        binding.btnEllipsize.setOnClickListener {
            shortToast(binding.edt.text.toString().ellipsize(5))
        }

        binding.btnSetBackgroundColor.setOnClickListener {
            binding.txt.text = binding.edt.text.setBackgroundColor(randomColor)
        }

        binding.btnSetForegroundColor.setOnClickListener {
            binding.txt.text = binding.edt.text.setForegroundColor(randomColor)
        }

        binding.btnHighlight.setOnClickListener {
            try{
                binding.txt.text = binding.edt.text.toString().highlight(key = "hello", bold = true, italic = true, color = randomColor, strikeLine = true, underline = true)
            }catch (e: Exception){}
        }

        binding.btnRandomString.setOnClickListener {
            binding.txt.text = randomString(4)
        }
    }
}