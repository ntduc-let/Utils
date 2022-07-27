package com.ntduc.utils.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ntduc.animationutils.*
import com.ntduc.utils.databinding.ActivityAnimUtilsBinding

class AnimUtilsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAnimUtilsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnFade.setOnClickListener {
            binding.textView.fade(initAlpha = 0.1f, alpha = 0.5f, startDelay = 3000)
        }

        binding.btnTranslate.setOnClickListener {
            binding.textView.translate(initX = 0, initY = 0, translationX = 100, translationY = 100)
        }

        binding.btnFadeInVertical.setOnClickListener {
            binding.textView.fadeInVertical()
        }

        binding.btnFadeOutVertical.setOnClickListener {
            binding.textView.fadeOutVertical()
        }

        binding.btnFadeInHorizontal.setOnClickListener {
            binding.textView.fadeInHorizontal()
        }

        binding.btnFadeOutHorizontal.setOnClickListener {
            binding.textView.fadeOutHorizontal()
        }

        binding.btnEaseInVertical.setOnClickListener {
            binding.textView2.easeInVertical()
        }

        binding.btnReveal.setOnClickListener {
            binding.textView.reveal(centerX = 0, centerY = 0)
        }
    }
}