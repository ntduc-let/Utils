package com.ntduc.audioplayerutils.player

import android.app.Activity
import android.view.WindowManager

class BrightnessControl(private val activity: Activity) {
    var currentBrightnessLevel = -1
    var screenBrightness: Float
        get() = activity.window.attributes.screenBrightness
        set(brightness) {
            val lp = activity.window.attributes
            lp.screenBrightness = brightness
            activity.window.attributes = lp
        }

    fun changeBrightness(
        playerView: CustomStyledPlayerView,
        increase: Boolean,
        canSetAuto: Boolean
    ) {
        val newBrightnessLevel =
            if (increase) currentBrightnessLevel + 1 else currentBrightnessLevel - 1
        if (canSetAuto && newBrightnessLevel < 0) currentBrightnessLevel =
            -1 else if (newBrightnessLevel in 0..30) currentBrightnessLevel =
            newBrightnessLevel
        if (currentBrightnessLevel == -1 && canSetAuto) screenBrightness =
            WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE else if (currentBrightnessLevel != -1) screenBrightness =
            levelToBrightness(currentBrightnessLevel)
        playerView.setHighlight(false)
        if (currentBrightnessLevel == -1 && canSetAuto) {
            playerView.setIconBrightnessAuto()
            playerView.setCustomErrorMessage("")
        } else {
            playerView.setIconBrightness()
            playerView.setCustomErrorMessage(" $currentBrightnessLevel")
        }
    }

    fun levelToBrightness(level: Int): Float {
        val d = 0.064 + 0.936 / 30.0 * level.toDouble()
        return (d * d).toFloat()
    }
}