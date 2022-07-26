package com.ntduc.viewpager2utils

import android.view.View

open class RotateDownTransformer : ABaseTransformer() {
    override val isPagingEnabled: Boolean
        get() = true

    override fun onTransform(page: View, position: Float) {
        val width = page.width.toFloat()
        val height = page.height.toFloat()
        val rotation = ROT_MOD * position

        page.pivotX = width * 0.5f
        page.pivotY = height
        page.rotation = rotation
    }

    companion object {
        private const val ROT_MOD = 18.75f
    }
}