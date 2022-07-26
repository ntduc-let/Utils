package com.ntduc.utils.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ntduc.contextutils.inflater
import com.ntduc.utils.adapter.FragmentAdapter
import com.ntduc.utils.databinding.ActivityViewPager2UtilsBinding
import com.ntduc.viewpager2utils.*

class ViewPager2UtilsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityViewPager2UtilsBinding.inflate(inflater)
        setContentView(binding.root)

        val adapter = FragmentAdapter(this)
        binding.pager.adapter = adapter
        binding.pager.setPageTransformer(AlphaTransformer())
    }
}