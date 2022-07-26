package com.ntduc.viewpager2utils

import android.view.View
import kotlin.math.abs
import kotlin.math.max

open class ZoomOutPageTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        val height = page.height.toFloat()
        val width = page.width.toFloat()

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.alpha  = 0f
        }
        else if (position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            val scaleFactor = max(MIN_SCALE, 1 - abs(position))
            val vertMargin = height * (1 - scaleFactor) / 2
            val horzMargin = width * (1 - scaleFactor) / 2
            page.translationX = if (position < 0) {
                horzMargin - vertMargin / 2
            } else {
                horzMargin + vertMargin / 2
            }

            // Scale the page down (between MIN_SCALE and 1)
            page.scaleX = scaleFactor
            page.scaleY = scaleFactor

            // Fade the page relative to its size.
            page.alpha = (MIN_ALPHA + (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
        }
        else { // (1,+Infinity]
            // This page is way off-screen to the right.
            page.alpha = 0f
        }
    }

    companion object {
        private const val MIN_SCALE = 0.85f
        private const val MIN_ALPHA = 0.5f
    }
}