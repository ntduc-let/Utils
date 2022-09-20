package com.ntduc.utils.file_utils.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.numberutils.formatBytes
import com.ntduc.utils.databinding.ItemVideoBinding
import com.ntduc.datetimeutils.formatAsTime
import com.ntduc.datetimeutils.getDateTimeFromMillis
import com.ntduc.utils.file_utils.model.MyVideo

class GetAllVideoAdapter(
    val context: Context,
    private var videos: List<MyVideo> = listOf()
) : RecyclerView.Adapter<GetAllVideoAdapter.ViewHolder>() {

    inner class ViewHolder(binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        internal val binding: ItemVideoBinding

        init {
            this.binding = binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = videos[position]
//
//        holder.binding.txtTitle.text = "Title: ${item.title}"
//        holder.binding.txtDisplayName.text = "DisplayName: ${item.displayName}"
//        holder.binding.txtMineType.text = "MineType: ${item.mimeType}"
//        holder.binding.txtSize.text = "Size: ${item.size?.formatBytes()}"
//        holder.binding.txtDateAdded.text =
//            "DateAdded: ${getDateTimeFromMillis(item.dateAdded ?: 0, "dd-MM-yyyy HH:mm:ss")}"
//        holder.binding.txtDateModified.text =
//            "DateModified: ${getDateTimeFromMillis(item.dateModified ?: 0, "dd-MM-yyyy HH:mm:ss")}"
//        holder.binding.txtData.text = "Data: ${item.data}"
        holder.binding.txtHeight.text = "Height: ${item.height}"
        holder.binding.txtWidth.text = "Width: ${item.width}"
        holder.binding.txtAlbum.text = "Album: ${item.album}"
        holder.binding.txtArtist.text = "Artist: ${item.artist}"
        holder.binding.txtDuration.text = "Duration: ${item.duration?.formatAsTime()}"
        holder.binding.txtBucketID.text = "BucketID: ${item.bucketID}"
        holder.binding.txtBucketDisplayName.text = "BucketDisplayName: ${item.bucketDisplayName}"
        holder.binding.txtResolution.text = "Resolution: ${item.resolution}"
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<MyVideo>) {
        videos = newList
        notifyDataSetChanged()
    }
}