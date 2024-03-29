package com.ntduc.utils.app_utils.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.ntduc.contextutils.inflater
import com.ntduc.utils.R
import com.ntduc.utils.app_utils.adapter.FragmentAdapter
import com.ntduc.utils.databinding.ActivityAppBinding

class AppActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppBinding
    private lateinit var adapter: FragmentAdapter
    private lateinit var viewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppBinding.inflate(inflater)
        setContentView(binding.root)

        init()
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadAllApp(this)
    }

    private fun init(){
        initView()
    }

    private fun initView() {
        viewModel = ViewModelProvider(this)[AppViewModel::class.java]

        adapter = FragmentAdapter(this)
        binding.viewPager.adapter = adapter
        TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager
        ) { tab, position ->
            when (position) {
                0 -> tab.text = "Installed App"
                1 -> tab.text = "Apk Package"
            }
        }.attach()
    }
}