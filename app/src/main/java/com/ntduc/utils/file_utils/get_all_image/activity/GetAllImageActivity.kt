package com.ntduc.utils.file_utils.get_all_image.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ntduc.utils.databinding.ActivityGetAllImageBinding
import com.ntduc.recyclerviewutils.sticky.StickyHeadersStaggeredGridLayoutManager
import com.ntduc.utils.file_utils.get_all_image.adapter.GetAllImageAdapter
import com.ntduc.utils.recycler_view_utils.sticky.RecyclerViewStickyActivity

class GetAllImageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGetAllImageBinding
    private lateinit var adapter: GetAllImageAdapter
    private lateinit var viewModel: GetAllImageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetAllImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadAllPhoto(this)
    }

    private fun init() {
        initView()
        initData()
    }

    private fun initData() {
        viewModel.listAllPhoto.observe(this) {
            if (viewModel.isLoadListAllPhoto) {
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
        viewModel = ViewModelProvider(this)[GetAllImageViewModel::class.java]

        adapter = GetAllImageAdapter(this)
        binding.rcvList.adapter = adapter
        val layoutManager: StickyHeadersStaggeredGridLayoutManager<RecyclerViewStickyActivity.MyAdapter> =
            StickyHeadersStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rcvList.layoutManager = layoutManager
    }
}