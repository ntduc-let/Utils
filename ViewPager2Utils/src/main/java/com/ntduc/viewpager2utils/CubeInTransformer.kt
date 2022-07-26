package com.ntduc.viewpager2utils

import android.view.View

open class CubeInTransformer : ABaseTransformer() {
    public override val isPagingEnabled: Boolean
        get() = true

    override fun onTransform(page: View, position: Float) {
        // Rotate the fragment on the left or right edge
        page.pivotX = if (position > 0) 0f else page.width.toFloat()
        page.pivotY = 0f
        page.rotationY = -90f * position
    }
}