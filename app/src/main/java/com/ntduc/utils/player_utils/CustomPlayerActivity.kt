package com.ntduc.utils.player_utils

import android.view.View
import com.ntduc.playerutils.player.PlayerActivity

class CustomPlayerActivity : PlayerActivity() {

    override fun getVisibilityFolderOpen(): Int {
        return View.GONE
    }
}