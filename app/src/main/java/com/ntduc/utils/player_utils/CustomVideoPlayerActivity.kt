package com.ntduc.utils.player_utils

import android.view.View
import androidx.core.content.ContextCompat
import com.ntduc.videoplayerutils.video.player.VideoPlayerActivity
import com.ntduc.toastutils.shortToast
import com.ntduc.utils.R

class CustomVideoPlayerActivity : VideoPlayerActivity() {

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

    override fun getPlayedColor(): Int {
        return ContextCompat.getColor(this, R.color.played_color)
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

    override fun getTextTimeSeparator(): String {
        return "/"
    }

    override fun getTextColorTimeSeparator(): Int {
        return ContextCompat.getColor(this, R.color.white)
    }

    override fun getTextColorTimeDuration(): Int {
        return ContextCompat.getColor(this, R.color.white)
    }

    override fun getVisibilityVolume(): Int {
        return View.VISIBLE
    }

    override fun isShowNextButton(): Boolean {
        return true
    }

    override fun isShowPreviousButton(): Boolean {
        return true
    }
}