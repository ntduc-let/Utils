package com.ntduc.viewpager2utils

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

open class DepthPageTransformer : ABaseTransformer() {
    override val isPagingEnabled: Boolean
        get() = true

    override fun onTransform(page: View, position: Float) {
        if (position <= 0f) {
            page.translationX = 0f
            page.scaleX = 1f
            page.scaleY = 1f
        } else if (position <= 1f) {
            val scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position))
            page.alpha = 1 - position
            page.pivotY = 0.5f * page.height
            page.translationX = page.width * -position
            page.scaleX = scaleFactor
            page.scaleY = scaleFactor
        }
    }

    companion object {
        private const val MIN_SCALE = 0.75f
    }
}