package com.ntduc.utils.file_utils.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.utils.databinding.ActivityGetAllImageBinding
import com.ntduc.utils.file_utils.adapter.GetAllImageAdapter
import com.ntduc.fileutils.getImages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GetAllImageActivity : AppCompatActivity() {
    private val activityScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGetAllImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = GetAllImageAdapter(this, listOf())
        binding.root.adapter = adapter
        binding.root.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        activityScope.launch(Dispatchers.IO){
            val files = getImages(types = listOf("jpg", "png"))
            launch(Dispatchers.Main){
                adapter.updateData(files)
            }
        }
    }
}