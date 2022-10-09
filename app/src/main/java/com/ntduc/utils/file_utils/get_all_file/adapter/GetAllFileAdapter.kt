package com.ntduc.utils.file_utils.get_all_file.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.ntduc.numberutils.formatBytes
import com.ntduc.recyclerviewutils.sticky.StickyHeaders
import com.ntduc.utils.R
import com.ntduc.utils.databinding.ItemDocumentBinding
import com.ntduc.utils.databinding.ItemHeaderBinding
import com.ntduc.utils.file_utils.constant.ExtensionConstants
import com.ntduc.utils.file_utils.constant.FileType
import com.ntduc.utils.model.MyFile
import com.ntduc.utils.model.MyFolderFile
import java.io.File
import java.util.ArrayList

class GetAllFileAdapter(
    val context: Context,
    private var listFolderFile: List<MyFolderFile> = listOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), StickyHeaders, StickyHeaders.ViewSetup {
    private var list: ArrayList<MyFile> = ArrayList()

    init {
        initData()
    }

    private fun initData() {
        listFolderFile.forEach { folder ->
            list.add(MyFile(title = "${folder.folder.title} (${folder.list.size})"))
            folder.list.forEach {
                list.add(it)
            }
        }
    }

    inner class ItemHeaderViewHolder(binding: ItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        internal val binding: ItemHeaderBinding

        init {
            this.binding = binding
        }
    }

    inner class ItemDocumentViewHolder(binding: ItemDocumentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        internal val binding: ItemDocumentBinding

        init {
            this.binding = binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == HEADER_ITEM) {
            val binding = ItemHeaderBinding.inflate(LayoutInflater.from(context), parent, false)
            ItemHeaderViewHolder(binding)
        } else {
            val binding = ItemDocumentBinding.inflate(LayoutInflater.from(context), parent, false)
            ItemDocumentViewHolder(binding)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]

        when (holder) {
            is ItemHeaderViewHolder -> {
                holder.binding.txtHeader.text = item.title
            }
            is ItemDocumentViewHolder -> {
                val file = File(item.data!!)
                if (file.isDirectory) {
                    holder.binding.img.setImageResource(R.drawable.ic_folder)
                } else {
                    var requestOptions = RequestOptions()
                    requestOptions = requestOptions.transform(CenterCrop(), RoundedCorners(16))

                    when (ExtensionConstants.getTypeFile(file.path)) {
                        FileType.IMAGE, FileType.VIDEO -> {
                            Glide.with(context)
                                .applyDefaultRequestOptions(RequestOptions())
                                .load(file.path)
                                .apply(requestOptions)
                                .placeholder(R.drawable.ic_empty)
                                .error(ExtensionConstants.getIconFile(item.data ?: ""))
                                .into(holder.binding.img)
                        }
                        FileType.MUSIC -> {
                            val image = try {
                                val mData = MediaMetadataRetriever()
                                mData.setDataSource(item.data)
                                val art = mData.embeddedPicture
                                BitmapFactory.decodeByteArray(art, 0, art!!.size)
                            } catch (e: Exception) {
                                null
                            }
                            Glide.with(context)
                                .applyDefaultRequestOptions(RequestOptions())
                                .load(image)
                                .apply(requestOptions)
                                .placeholder(R.drawable.ic_empty)
                                .error(ExtensionConstants.getIconFile(item.data ?: ""))
                                .into(holder.binding.img)
                        }
                        else -> {
                            holder.binding.img.setImageResource(
                                ExtensionConstants.getIconFile(
                                    item.data ?: ""
                                )
                            )
                        }
                    }
                }

                holder.binding.txtTitle.text = item.displayName
                holder.binding.txtDescription.text = item.size?.formatBytes()
                holder.binding.root.setOnClickListener {
                    onOpenListener?.let {
                        it(item)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].data == null) HEADER_ITEM else super.getItemViewType(
            position
        )
    }

    override fun isStickyHeader(position: Int): Boolean {
        return getItemViewType(position) == HEADER_ITEM
    }

    override fun setupStickyHeaderView(stickyHeader: View) {
        ViewCompat.setElevation(stickyHeader, 0F)
    }

    override fun teardownStickyHeaderView(stickyHeader: View) {
        ViewCompat.setElevation(stickyHeader, 0F)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val lp: ViewGroup.LayoutParams = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            if (isStickyHeader(holder.layoutPosition)) {
                val p: StaggeredGridLayoutManager.LayoutParams = lp
                p.isFullSpan = true
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<MyFolderFile>) {
        listFolderFile = newList
        initData()
        notifyDataSetChanged()
    }

    companion object {
        private const val HEADER_ITEM = 123
    }

    private var onOpenListener: ((MyFile) -> Unit)? = null

    fun setOnOpenListener(listener: (MyFile) -> Unit) {
        onOpenListener = listener
    }
}