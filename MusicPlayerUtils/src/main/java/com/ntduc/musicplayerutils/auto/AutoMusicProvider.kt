/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package com.ntduc.musicplayerutils.auto

import android.content.Context
import android.content.res.Resources
import android.support.v4.media.MediaBrowserCompat
import com.ntduc.musicplayerutils.R
import com.ntduc.musicplayerutils.helper.MusicPlayerRemote
import com.ntduc.musicplayerutils.model.Song
import com.ntduc.musicplayerutils.repository.AlbumRepository
import com.ntduc.musicplayerutils.repository.ArtistRepository
import com.ntduc.musicplayerutils.repository.PlaylistRepository
import com.ntduc.musicplayerutils.repository.SongRepository
import com.ntduc.musicplayerutils.service.MusicService
import com.ntduc.musicplayerutils.utils.MusicUtil
import com.ntduc.musicplayerutils.utils.PreferenceUtil
import java.lang.ref.WeakReference


/**
 * Created by Beesham Sarendranauth (Beesham)
 */
class AutoMusicProvider(
    private val mContext: Context,
    private val songsRepository: SongRepository,
    private val albumsRepository: AlbumRepository,
    private val artistsRepository: ArtistRepository,
    private val playlistsRepository: PlaylistRepository
) {
    private var mMusicService: WeakReference<MusicService>? = null

    fun setMusicService(service: MusicService) {
        mMusicService = WeakReference(service)
    }

    fun getChildren(mediaId: String?, resources: Resources): MutableList<MediaBrowserCompat.MediaItem> {
        val mediaItems: MutableList<MediaBrowserCompat.MediaItem> = ArrayList()
        when (mediaId) {
            AutoMediaIDHelper.MEDIA_ID_ROOT -> {
                mediaItems.addAll(getRootChildren(resources))
            }
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST -> for (playlist in playlistsRepository.playlists()) {
                mediaItems.add(
                    AutoMediaItem.with(mContext)
                        .path(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_PLAYLIST, playlist.id)
                        .icon(R.drawable.ic_playlist_play)
                        .title(playlist.name)
                        .subTitle(playlist.getInfoString(mContext))
                        .asPlayable()
                        .build()
                )
            }
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM -> for (album in albumsRepository.albums()) {
                mediaItems.add(
                    AutoMediaItem.with(mContext)
                        .path(mediaId, album.id)
                        .title(album.title)
                        .subTitle(album.albumArtist ?: album.artistName)
                        .icon(MusicUtil.getMediaStoreAlbumCoverUri(album.id))
                        .asPlayable()
                        .build()
                )
            }
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ARTIST -> for (artist in artistsRepository.artists()) {
                mediaItems.add(
                    AutoMediaItem.with(mContext)
                        .asPlayable()
                        .path(mediaId, artist.id)
                        .title(artist.name)
                        .build()
                )
            }
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_ALBUM_ARTIST -> for (artist in artistsRepository.albumArtists()) {
                mediaItems.add(
                    AutoMediaItem.with(mContext)
                        .asPlayable()
                        // we just pass album id here as we don't have album artist id's
                        .path(mediaId, artist.safeGetFirstAlbum().id)
                        .title(artist.name)
                        .build()
                )
            }
            AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE ->
                mMusicService?.get()?.playingQueue
                    ?.let {
                        for (song in it) {
                            mediaItems.add(
                                AutoMediaItem.with(mContext)
                                    .asPlayable()
                                    .path(mediaId, song.id)
                                    .title(song.title)
                                    .subTitle(song.artistName)
                                    .icon(MusicUtil.getMediaStoreAlbumCoverUri(song.albumId))
                                    .build()
                            )
                        }
                    }
            else -> {
                getPlaylistChildren(mediaId, mediaItems)
            }
        }
        return mediaItems
    }

    private fun getPlaylistChildren(
        mediaId: String?,
        mediaItems: MutableList<MediaBrowserCompat.MediaItem>
    ) {
    }

    private fun getRootChildren(resources: Resources): List<MediaBrowserCompat.MediaItem> {
        val mediaItems: MutableList<MediaBrowserCompat.MediaItem> = ArrayList()
        mediaItems.add(
            AutoMediaItem.with(mContext)
                .asPlayable()
                .path(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_SHUFFLE)
                .icon(R.drawable.ic_shuffle)
                .title("Shuffle all")
                .subTitle(MusicUtil.getPlaylistInfoString(mContext, songsRepository.songs()))
                .build()
        )
        mediaItems.add(
            AutoMediaItem.with(mContext)
                .asBrowsable()
                .path(AutoMediaIDHelper.MEDIA_ID_MUSICS_BY_QUEUE)
                .icon(R.drawable.ic_queue_music)
                .title("Playing Queue")
                .subTitle(MusicUtil.getPlaylistInfoString(mContext, MusicPlayerRemote.playingQueue))
                .asBrowsable().build()
        )
        return mediaItems
    }

    private fun getPlayableSong(mediaId: String?, song: Song): MediaBrowserCompat.MediaItem {
        return AutoMediaItem.with(mContext)
            .asPlayable()
            .path(mediaId, song.id)
            .title(song.title)
            .subTitle(song.artistName)
            .icon(MusicUtil.getMediaStoreAlbumCoverUri(song.albumId))
            .build()
    }
}