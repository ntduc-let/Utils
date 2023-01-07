package com.ntduc.musicplayerutils.model

import android.content.Context
import android.os.Parcelable
import com.ntduc.musicplayerutils.MusicApp
import com.ntduc.musicplayerutils.repository.RealPlaylistRepository
import com.ntduc.musicplayerutils.utils.MusicUtil
import kotlinx.parcelize.Parcelize

@Parcelize
open class Playlist(
    val id: Long,
    val name: String
) : Parcelable {

    companion object {
        val empty = Playlist(-1, "")
    }

    // this default implementation covers static playlists
    fun getSongs(): List<Song> {
        return RealPlaylistRepository(MusicApp.getContext().contentResolver).playlistSongs(id)
    }

    open fun getInfoString(context: Context): String {
        val songCount = getSongs().size
        val songCountString = MusicUtil.getSongCountString(context, songCount)
        return MusicUtil.buildInfoString(
            songCountString,
            ""
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Playlist

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}