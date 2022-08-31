package com.ntduc.utils.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.numberutils.formatBytes
import com.ntduc.utils.databinding.ItemFileBinding
import com.prox.datetimeutils.getDateTimeFromMillis
import com.prox.fileutils.model.BaseFile

class GetAllFileAdapter(
    val context: Context,
    private var files: List<BaseFile> = listOf()
) : RecyclerView.Adapter<GetAllFileAdapter.ViewHolder>() {

    inner class ViewHolder(binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {
        internal val binding: ItemFileBinding

        init {
            this.binding = binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = files[position]

        holder.binding.txtTitle.text = "Title: ${item.title}"
        holder.binding.txtDisplayName.text = "DisplayName: ${item.displayName}"
        holder.binding.txtMineType.text = "MineType: ${item.mimeType}"
        holder.binding.txtSize.text = "Size: ${item.size.formatBytes()}"
        holder.binding.txtDateAdded.text = "DateAdded: ${getDateTimeFromMillis(item.dateAdded*1000, "dd-MM-yyyy HH:mm:ss")}"
        holder.binding.txtDateModified.text = "DateModified: ${getDateTimeFromMillis(item.dateModified*1000, "dd-MM-yyyy HH:mm:ss")}"
        holder.binding.txtData.text = "Data: ${item.data}"
    }

    override fun getItemCount(): Int {
        return files.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<BaseFile>) {
        files = newList
        notifyDataSetChanged()
    }
}