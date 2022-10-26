package com.ntduc.playerutils.file_chooser.internals

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import com.ntduc.playerutils.file_chooser.internals.UiUtil.dip2px

class WrappedDrawable(private val drawable: Drawable, widthInDp: Float, heightInDp: Float) :
    Drawable() {

    init {
        setBounds(0, 0, dip2px(widthInDp).toInt(), dip2px(heightInDp).toInt())
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        //update bounds to get correctly
        super.setBounds(left, top, right, bottom)
        val drawable: Drawable = drawable
        drawable.setBounds(left, top, right, bottom)
    }

    override fun setAlpha(alpha: Int) {
        val drawable: Drawable = drawable
        drawable.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        val drawable: Drawable = drawable
        drawable.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        val drawable: Drawable = drawable
        return drawable.opacity
    }

    override fun draw(canvas: Canvas) {
        val drawable: Drawable = drawable
        drawable.draw(canvas)
    }

    override fun getIntrinsicWidth(): Int {
        val drawable: Drawable = drawable
        return drawable.bounds.width()
    }

    override fun getIntrinsicHeight(): Int {
        val drawable: Drawable = drawable
        return drawable.bounds.height()
    }
}