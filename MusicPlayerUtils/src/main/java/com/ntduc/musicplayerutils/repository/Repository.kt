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

package com.ntduc.musicplayerutils.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.ntduc.musicplayerutils.model.Album
import com.ntduc.musicplayerutils.model.Artist
import com.ntduc.musicplayerutils.model.Playlist
import com.ntduc.musicplayerutils.model.Song

interface Repository {
    fun albumById(albumId: Long): Album
    suspend fun fetchAlbums(): List<Album>
    suspend fun albumByIdAsync(albumId: Long): Album
    suspend fun allSongs(): List<Song>
    suspend fun fetchArtists(): List<Artist>
    suspend fun albumArtists(): List<Artist>
    suspend fun fetchLegacyPlaylist(): List<Playlist>
    suspend fun artistById(artistId: Long): Artist
    suspend fun albumArtistByName(name: String): Artist
    suspend fun playlist(playlistId: Long): Playlist
    suspend fun searchArtists(query: String): List<Artist>
    suspend fun searchSongs(query: String): List<Song>
    suspend fun searchAlbums(query: String): List<Album>
}

class RealRepository(
    private val context: Context,
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository,
    private val playlistRepository: PlaylistRepository
) : Repository {

    override suspend fun searchSongs(query: String): List<Song> = songRepository.songs(query)

    override suspend fun searchAlbums(query: String): List<Album> = albumRepository.albums(query)

    override suspend fun searchArtists(query: String): List<Artist> =
        artistRepository.artists(query)

    override suspend fun fetchAlbums(): List<Album> = albumRepository.albums()

    override suspend fun albumByIdAsync(albumId: Long): Album = albumRepository.album(albumId)

    override fun albumById(albumId: Long): Album = albumRepository.album(albumId)

    override suspend fun fetchArtists(): List<Artist> = artistRepository.artists()

    override suspend fun albumArtists(): List<Artist> = artistRepository.albumArtists()

    override suspend fun artistById(artistId: Long): Artist = artistRepository.artist(artistId)

    override suspend fun albumArtistByName(name: String): Artist =
        artistRepository.albumArtist(name)

    override suspend fun fetchLegacyPlaylist(): List<Playlist> = playlistRepository.playlists()

    override suspend fun allSongs(): List<Song> = songRepository.songs()

    override suspend fun playlist(playlistId: Long) =
        playlistRepository.playlist(playlistId)
}