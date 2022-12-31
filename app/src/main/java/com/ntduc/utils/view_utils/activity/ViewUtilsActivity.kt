package com.ntduc.utils.view_utils.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ntduc.contextutils.inflater
import com.ntduc.utils.databinding.ActivityViewUtilsBinding
import com.ntduc.viewutils.*

class ViewUtilsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewUtilsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewUtilsBinding.inflate(inflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        initView()
        initEvent()
    }

    private fun initView() {
        binding.image.setRippleClickAnimation()
    }

    private fun initEvent() {
        binding.btnRotateAnimation.setOnClickListener {
            binding.image.rotateAnimation(180f, 1000)
        }

        binding.btnBlink.setOnClickListener {
            binding.image.blink(1000)
        }
    }
}