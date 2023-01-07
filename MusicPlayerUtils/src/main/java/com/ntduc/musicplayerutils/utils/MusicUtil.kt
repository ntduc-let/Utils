package com.ntduc.musicplayerutils.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toUri
import com.ntduc.musicplayerutils.model.Artist
import com.ntduc.musicplayerutils.model.Song
import com.ntduc.musicplayerutils.repository.RealRepository
import java.util.*


object MusicUtil {

    @JvmStatic
    fun getMediaStoreAlbumCoverUri(albumId: Long): Uri {
        val sArtworkUri = "content://media/external/audio/albumart".toUri()
        return ContentUris.withAppendedId(sArtworkUri, albumId)
    }

    fun getSongFileUri(songId: Long): Uri {
        return ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            songId
        )
    }

    fun isVariousArtists(artistName: String?): Boolean {
        if (artistName.isNullOrEmpty()) {
            return false
        }
        if (artistName == Artist.VARIOUS_ARTISTS_DISPLAY_NAME) {
            return true
        }
        return false
    }

    fun isArtistNameUnknown(artistName: String?): Boolean {
        if (artistName.isNullOrEmpty()) {
            return false
        }
        if (artistName == Artist.UNKNOWN_ARTIST_DISPLAY_NAME) {
            return true
        }
        val tempName = artistName.trim { it <= ' ' }.lowercase()
        return tempName == "unknown" || tempName == "<unknown>"
    }

    fun getSongCountString(context: Context, songCount: Int): String {
        val songString = if (songCount == 1) "Song" else "Songs"
        return "$songCount $songString"
    }

    fun buildInfoString(string1: String?, string2: String?): String {
        if (string1.isNullOrEmpty()) {
            return if (string2.isNullOrEmpty()) "" else string2
        }
        return if (string2.isNullOrEmpty()) if (string1.isNullOrEmpty()) "" else string1 else "$string1  •  $string2"
    }

    fun getPlaylistInfoString(
        context: Context,
        songs: List<Song>,
    ): String {
        val duration = getTotalDuration(songs)
        return buildInfoString(
            getSongCountString(context, songs.size),
            getReadableDurationString(duration)
        )
    }

    fun getTotalDuration(songs: List<Song>): Long {
        var duration: Long = 0
        for (i in songs.indices) {
            duration += songs[i].duration
        }
        return duration
    }

    fun getReadableDurationString(songDurationMillis: Long): String {
        var minutes = songDurationMillis / 1000 / 60
        val seconds = songDurationMillis / 1000 % 60
        return if (minutes < 60) {
            String.format(
                Locale.getDefault(),
                "%02d:%02d",
                minutes,
                seconds
            )
        } else {
            val hours = minutes / 60
            minutes %= 60
            String.format(
                Locale.getDefault(),
                "%02d:%02d:%02d",
                hours,
                minutes,
                seconds
            )
        }
    }

    private val repository = RealRepository()
    suspend fun isFavorite(song: Song) = repository.isSongFavorite(song.id)
}