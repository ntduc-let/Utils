package com.ntduc.utils.file_utils.get_all_audio.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.ntduc.utils.databinding.ActivityGetAllAudioBinding
import com.ntduc.recyclerviewutils.sticky.StickyHeadersLinearLayoutManager
import com.ntduc.utils.file_utils.get_all_audio.adapter.GetAllAudioAdapter
import com.ntduc.utils.recycler_view_utils.sticky.RecyclerViewStickyActivity

class GetAllAudioActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGetAllAudioBinding
    private lateinit var adapter: GetAllAudioAdapter
    private lateinit var viewModel: GetAllAudioViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetAllAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadAllAudio(this)
    }

    private fun init() {
        initView()
        initData()
    }

    private fun initData() {
        viewModel.listAllAudio.observe(this) {
            if (viewModel.isLoadListAllAudio) {
                binding.layoutLoading.root.visibility = View.GONE
                if (it.isEmpty()) {
                    binding.layoutNoItem.root.visibility = View.VISIBLE
                    binding.rcvList.visibility = View.INVISIBLE
                } else {
                    binding.layoutNoItem.root.visibility = View.GONE
                    binding.rcvList.visibility = View.VISIBLE
                    adapter.updateData(it)
                }
            } else {
                binding.layoutLoading.root.visibility = View.VISIBLE
            }
        }
    }

    private fun initView() {
        viewModel = ViewModelProvider(this)[GetAllAudioViewModel::class.java]

        adapter = GetAllAudioAdapter(this)
        binding.rcvList.adapter = adapter
        binding.rcvList.setHasFixedSize(true)
        val layoutManager: StickyHeadersLinearLayoutManager<RecyclerViewStickyActivity.MyAdapter> =
            StickyHeadersLinearLayoutManager(this)
        binding.rcvList.layoutManager = layoutManager
    }
}