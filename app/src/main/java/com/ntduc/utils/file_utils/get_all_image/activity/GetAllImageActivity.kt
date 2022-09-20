package com.ntduc.utils.file_utils.get_all_image.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.utils.databinding.ActivityGetAllImageBinding
import com.ntduc.fileutils.getImages
import com.ntduc.utils.file_utils.get_all_image.adapter.GetAllImageAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class GetAllImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGetAllImageBinding
    private lateinit var adapter: GetAllImageAdapter
    private lateinit var viewModel: GetAllImageViewModel

    private val activityScope = CoroutineScope(Job() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetAllImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()

//        adapter = GetAllImageAdapter(this, listOf())
//        binding.root.adapter = adapter
//        binding.root.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        activityScope.launch(Dispatchers.IO){
            val files = getImages(types = listOf("jpg", "png"))
            launch(Dispatchers.Main){
//                adapter.updateData(files)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadAllPhoto(this)
    }

    private fun init(){
        initView()
        initData()
    }

    private fun initData() {
        adapter = GetAllImageAdapter(this, listOf())
//        binding.root.adapter = adapter
//        binding.root.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun initView() {
        adapter = GetAllImageAdapter(this, listOf())
        viewModel = ViewModelProvider(this)[GetAllImageViewModel::class.java]
    }
}