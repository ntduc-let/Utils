package com.ntduc.utils.file_utils.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ntduc.contextutils.inflater
import com.ntduc.utils.databinding.ActivityRecyclerUtilsBinding

class RecyclerUtilsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecyclerUtilsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerUtilsBinding.inflate(inflater)
        setContentView(binding.root)

        binding.btnRecyclerViewSticky.setOnClickListener {
            startActivity(Intent(this, RecyclerViewStickyActivity::class.java))
        }
    }
}