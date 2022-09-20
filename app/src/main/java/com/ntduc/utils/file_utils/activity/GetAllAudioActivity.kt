package com.ntduc.utils.file_utils.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.utils.databinding.ActivityGetAllAudioBinding
import com.ntduc.utils.file_utils.adapter.GetAllAudioAdapter
import com.ntduc.fileutils.getAudios
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GetAllAudioActivity : AppCompatActivity() {
    private val activityScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGetAllAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = GetAllAudioAdapter(this, listOf())
        binding.root.adapter = adapter
        binding.root.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        activityScope.launch(Dispatchers.IO){
            val files = getAudios(types = listOf("mp3"))
            launch(Dispatchers.Main){
//                adapter.updateData(files)
            }
        }
    }
}