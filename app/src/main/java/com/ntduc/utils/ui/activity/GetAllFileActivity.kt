package com.ntduc.utils.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.utils.databinding.ActivityGetAllFileBinding
import com.ntduc.utils.ui.adapter.GetAllFileAdapter
import com.prox.fileutils.getFiles
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GetAllFileActivity : AppCompatActivity() {
    private val activityScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGetAllFileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = GetAllFileAdapter(this, listOf())
        binding.root.adapter = adapter
        binding.root.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)


        activityScope.launch(Dispatchers.IO){
            val files = getFiles(types = listOf("doc", "docx", "ppt", "pptx", "xls", "xlsx", "pdf"))
            launch(Dispatchers.Main){
                adapter.updateData(files)
            }
        }
    }
}