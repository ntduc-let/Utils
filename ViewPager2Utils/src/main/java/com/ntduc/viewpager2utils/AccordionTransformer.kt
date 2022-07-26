package com.ntduc.viewpager2utils

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class AccordionTransformer : ABaseTransformer() {
    override fun onTransform(page: View, position: Float) {
        page.pivotX = if (position < 0) 0f else page.width.toFloat()
        page.scaleX = if (position < 0) 1f + position else 1f - position
    }
}