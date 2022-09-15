package com.ntduc.utils.file_utils.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.numberutils.formatBytes
import com.ntduc.utils.databinding.ItemImageBinding
import com.ntduc.datetimeutils.getDateTimeFromMillis
import com.ntduc.fileutils.model.BaseImage

class GetAllImageAdapter(
    val context: Context,
    private var images: List<BaseImage> = listOf()
) : RecyclerView.Adapter<GetAllImageAdapter.ViewHolder>() {

    inner class ViewHolder(binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        internal val binding: ItemImageBinding

        init {
            this.binding = binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = images[position]

        holder.binding.txtTitle.text = "Title: ${item.title}"
        holder.binding.txtDisplayName.text = "DisplayName: ${item.displayName}"
        holder.binding.txtMineType.text = "MineType: ${item.mimeType}"
        holder.binding.txtSize.text = "Size: ${item.size?.formatBytes()}"
        holder.binding.txtDateAdded.text =
            "DateAdded: ${getDateTimeFromMillis(item.dateAdded ?: 0, "dd-MM-yyyy HH:mm:ss")}"
        holder.binding.txtDateModified.text =
            "DateModified: ${getDateTimeFromMillis(item.dateModified ?: 0, "dd-MM-yyyy HH:mm:ss")}"
        holder.binding.txtData.text = "Data: ${item.data}"
        holder.binding.txtHeight.text = "Height: ${item.height}"
        holder.binding.txtWidth.text = "Width: ${item.width}"
    }

    override fun getItemCount(): Int {
        return images.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<BaseImage>) {
        images = newList
        notifyDataSetChanged()
    }
}