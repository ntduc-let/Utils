package com.ntduc.utils.file_utils.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.utils.databinding.ActivityGetAllVideoBinding
import com.ntduc.utils.file_utils.adapter.GetAllVideoAdapter
import com.ntduc.fileutils.getVideos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GetAllVideoActivity : AppCompatActivity() {
    private val activityScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGetAllVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = GetAllVideoAdapter(this, listOf())
        binding.root.adapter = adapter
        binding.root.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        activityScope.launch(Dispatchers.IO){
            val files = getVideos(types = listOf("mp4"))
            launch(Dispatchers.Main){
                adapter.updateData(files)
            }
        }
    }
}