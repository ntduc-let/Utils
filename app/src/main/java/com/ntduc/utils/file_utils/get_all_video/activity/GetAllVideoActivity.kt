package com.ntduc.utils.file_utils.get_all_video.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.ntduc.fileutils.getMimeType
import com.ntduc.fileutils.openFile
import com.ntduc.playerutils.player.PlayerActivity
import com.ntduc.recyclerviewutils.sticky.StickyHeadersGridLayoutManager
import com.ntduc.toastutils.shortToast
import com.ntduc.utils.databinding.ActivityGetAllVideoBinding
import com.ntduc.utils.file_utils.constant.ExtensionConstants
import com.ntduc.utils.file_utils.constant.FileType
import com.ntduc.utils.file_utils.get_all_video.adapter.GetAllVideoAdapter
import com.ntduc.utils.player_utils.CustomPlayerActivity
import java.io.File

class GetAllVideoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGetAllVideoBinding
    private lateinit var adapter: GetAllVideoAdapter
    private lateinit var viewModel: GetAllVideoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGetAllVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadAllVideo(this)
    }

    private fun init() {
        initView()
        initData()
        initEvent()
    }

    private fun initEvent() {
        adapter.setOnOpenListener {
            if (it.myFile?.data != null && File(it.myFile!!.data!!).exists()) {
                val file = File(it.myFile!!.data!!)
                val uri = FileProvider.getUriForFile(this, "com.ntduc.utils.provider", file)
                val intentOpenVideo = Intent(this, CustomPlayerActivity::class.java)
                intentOpenVideo.setDataAndType(uri, file.getMimeType())
                intentOpenVideo.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intentOpenVideo)
            } else {
                shortToast("File does not exists")
            }
        }
    }

    private fun initData() {
        viewModel.listAllVideo.observe(this) {
            if (viewModel.isLoadListAllVideo) {
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
        viewModel = ViewModelProvider(this)[GetAllVideoViewModel::class.java]

        adapter = GetAllVideoAdapter(this)
        binding.rcvList.adapter = adapter
        binding.rcvList.setHasFixedSize(true)
        val layoutManager: StickyHeadersGridLayoutManager<GetAllVideoAdapter> =
            StickyHeadersGridLayoutManager(this, 3)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.isStickyHeader(position)) {
                    3
                } else 1
            }
        }
        binding.rcvList.layoutManager = layoutManager
    }
}