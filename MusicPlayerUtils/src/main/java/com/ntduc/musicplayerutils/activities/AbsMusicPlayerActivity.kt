/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.ntduc.musicplayerutils.activities

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.lifecycleScope
import com.ntduc.musicplayerutils.activities.base.AbsMusicServiceActivity
import com.ntduc.musicplayerutils.helper.MusicPlayerRemote
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AbsMusicPlayerActivity : AbsMusicServiceActivity() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        intent ?: return
        handlePlaybackIntent(intent)
    }

    private fun handlePlaybackIntent(intent: Intent) {
//        lifecycleScope.launch(IO) {
//            val uri: Uri? = intent.data
//            var handled = false
//            if (uri != null && uri.toString().isNotEmpty()) {
//                val list = arrayListOf<Uri>()
//                list.add(uri)
//                MusicPlayerRemote.openQueue(list, 0, true)
//                handled = true
//            }
//            if (handled) {
//                setIntent(Intent())
//            }
//        }
    }

    companion object{
        const val API_PLAYLIST = "API_PLAYLIST"
        const val API_CURRENT_PATH = "API_CURRENT_PATH"
    }
}
