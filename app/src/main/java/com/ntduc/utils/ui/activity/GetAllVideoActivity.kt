package com.ntduc.utils.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.utils.databinding.ActivityGetAllFileBinding
import com.ntduc.utils.databinding.ActivityGetAllVideoBinding
import com.ntduc.utils.ui.adapter.GetAllFileAdapter
import com.ntduc.utils.ui.adapter.GetAllVideoAdapter
import com.prox.fileutils.getFiles
import com.prox.fileutils.getVideos
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
            val files = getVideos(listOf("mp4"))
            launch(Dispatchers.Main){
                adapter.updateData(files)
            }
        }
    }
}