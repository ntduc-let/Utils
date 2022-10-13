package com.ntduc.utils.player_utils

import android.view.View
import com.ntduc.playerutils.player.PlayerActivity
import com.ntduc.toastutils.shortToast
import com.ntduc.utils.R

class CustomPlayerActivity : PlayerActivity() {

    override fun getVisibilityFolderOpen(): Int {
        return View.GONE
    }

    override fun getVisibilityPictureInPictureAlt(): Int {
        return View.GONE
    }

    override fun getDrawableResAspectRatioFill(): Int {
        return R.drawable.ic_fill_24dp
    }

    override fun getDrawableResAspectRatioZoom(): Int {
        return R.drawable.ic_zoom_24dp
    }

    override fun getVisibilityRotation(): Int {
        return View.GONE
    }

    override fun enterMore(view: View) {
        shortToast("Click More")
    }

    override fun getVisibilitySubtitle(): Int {
        return View.GONE
    }

    override fun getVisibilitySettings(): Int {
        return View.GONE
    }

    override fun getVisibilityRepeat(): Int {
        return View.GONE
    }
}