package com.ntduc.utils.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.numberutils.formatBytes
import com.ntduc.utils.databinding.ItemMusicBinding
import com.prox.datetimeutils.formatAsTime
import com.prox.datetimeutils.getDateTimeFromMillis
import com.prox.fileutils.model.BaseAudio

class GetAllAudioAdapter(
    val context: Context,
    private var audios: List<BaseAudio> = listOf()
) : RecyclerView.Adapter<GetAllAudioAdapter.ViewHolder>() {

    inner class ViewHolder(binding: ItemMusicBinding) : RecyclerView.ViewHolder(binding.root) {
        internal val binding: ItemMusicBinding

        init {
            this.binding = binding
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMusicBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = audios[position]

        holder.binding.txtTitle.text = "Title: ${item.title}"
        holder.binding.txtDisplayName.text = "DisplayName: ${item.displayName}"
        holder.binding.txtMineType.text = "MineType: ${item.mineType}"
        holder.binding.txtSize.text = "Size: ${item.size.formatBytes()}"
        holder.binding.txtDateAdded.text = "DateAdded: ${getDateTimeFromMillis(item.dateAdded*1000, "dd-MM-yyyy HH:mm:ss")}"
        holder.binding.txtDateModified.text = "DateModified: ${getDateTimeFromMillis(item.dateModified*1000, "dd-MM-yyyy HH:mm:ss")}"
        holder.binding.txtData.text = "Data: ${item.data}"
        holder.binding.txtAlbum.text = "Album: ${item.album}"
        holder.binding.txtArtist.text = "Artist: ${item.artist}"
        holder.binding.txtDuration.text = "Duration: ${item.duration.formatAsTime()}"
    }

    override fun getItemCount(): Int {
        return audios.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<BaseAudio>) {
        audios = newList
        notifyDataSetChanged()
    }
}