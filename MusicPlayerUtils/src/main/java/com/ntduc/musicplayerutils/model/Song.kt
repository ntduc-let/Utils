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
package com.ntduc.musicplayerutils.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// update equals and hashcode if fields changes
@Parcelize
open class Song(
    var id: Long? = null,
    var title: String? = null,
    var trackNumber: Int? = null,
    var year: Int? = null,
    var duration: Long? = null,
    var data: String? = null,
    var dateModified: Long? = null,
    var albumId: Long? = null,
    var albumName: String? = null,
    var artistId: Long? = null,
    var artistName: String? = null,
    var composer: String? = null,
    var albumArtist: String? = null
) : Parcelable {

    companion object {
        @JvmStatic
        val emptySong = Song(
            id = -1,
            title = "",
            trackNumber = -1,
            year = -1,
            duration = -1,
            data = "",
            dateModified = -1,
            albumId = -1,
            albumName = "",
            artistId = -1,
            artistName = "",
            composer = "",
            albumArtist = ""
        )
    }
}