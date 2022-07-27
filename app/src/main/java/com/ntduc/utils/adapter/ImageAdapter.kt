package com.ntduc.utils.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.utils.databinding.ItemImageBinding
import kotlin.collections.ArrayList

class ImageAdapter(
    val context: Context,
    var listImage: List<Int> = ArrayList()
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    inner class ViewHolder(binding: ItemImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        internal val binding: ItemImageBinding

        init {
            this.binding = binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.root.setImageResource(listImage[position])
    }

    override fun getItemCount(): Int {
        return listImage.size
    }
}