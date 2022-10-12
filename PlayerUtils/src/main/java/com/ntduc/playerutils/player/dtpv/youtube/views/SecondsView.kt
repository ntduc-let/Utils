package com.ntduc.playerutils.player.dtpv.youtube.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.util.Consumer
import com.ntduc.playerutils.R

/**
 * Layout group which handles the icon animation while forwarding and rewinding.
 *
 * Since it's based on view's alpha the fading effect is more fluid (more YouTube-like) than
 * using static drawables, especially when [cycleDuration] is low.
 *
 * Used by [YouTubeOverlay][com.github.vkay94.dtpv.youtube.YouTubeOverlay].
 */
@SuppressLint("CutPasteId")
class SecondsView(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    private var cycleDuration = 750L
    private var seconds = 0
    private var isForward = true
    private var icon: Int
    private var animate: Boolean
    private var firstAnimator: ValueAnimator? = null
    private var secondAnimator: ValueAnimator? = null
    private var thirdAnimator: ValueAnimator? = null
    private var fourthAnimator: ValueAnimator? = null
    private var fifthAnimator: ValueAnimator? = null

    init {
        icon = R.drawable.ic_play_triangle
        animate = false
        LayoutInflater.from(context).inflate(R.layout.yt_seconds_view, this, true)
        firstAnimator = CustomValueAnimator({
            findViewById<View>(R.id.icon_1).alpha = 0f
            findViewById<View>(R.id.icon_2).alpha = 0f
            findViewById<View>(R.id.icon_3).alpha = 0f
        },
            { aFloat ->
                findViewById<View>(R.id.icon_1).alpha = aFloat ?: 0f
            }) { if (animate) secondAnimator?.start() }
        secondAnimator = CustomValueAnimator({
            findViewById<View>(R.id.icon_1).alpha = 1f
            findViewById<View>(R.id.icon_2).alpha = 0f
            findViewById<View>(R.id.icon_3).alpha = 0f
        },
            { aFloat ->
                findViewById<View>(R.id.icon_2).alpha = aFloat ?: 0f
            }) { if (animate) thirdAnimator?.start() }
        thirdAnimator = CustomValueAnimator({
            findViewById<View>(R.id.icon_1).alpha = 1f
            findViewById<View>(R.id.icon_2).alpha = 1f
            findViewById<View>(R.id.icon_3).alpha = 0f
        }, { aFloat ->
            findViewById<View>(R.id.icon_1).alpha = 1f - findViewById<View>(R.id.icon_3).alpha
            findViewById<View>(R.id.icon_3).alpha = aFloat ?: 0f
        }) { if (animate) fourthAnimator?.start() }
        fourthAnimator = CustomValueAnimator({
            findViewById<View>(R.id.icon_1).alpha = 0f
            findViewById<View>(R.id.icon_2).alpha = 1f
            findViewById<View>(R.id.icon_3).alpha = 1f
        },
            { aFloat ->
                findViewById<View>(R.id.icon_2).alpha = 1f - (aFloat ?: 0f)
            }) { if (animate) fifthAnimator?.start() }
        fifthAnimator = CustomValueAnimator({
            findViewById<View>(R.id.icon_1).alpha = 0f
            findViewById<View>(R.id.icon_2).alpha = 0f
            findViewById<View>(R.id.icon_3).alpha = 1f
        },
            { aFloat ->
                findViewById<View>(R.id.icon_3).alpha = 1f - (aFloat ?: 0f)
            }) { if (animate) firstAnimator?.start() }
    }

    /**
     * Defines the duration for a full cycle of the triangle animation.
     * Each animation step takes 20% of it.
     */
    fun getCycleDuration(): Long {
        return cycleDuration
    }

    fun setCycleDuration(value: Long) {
        firstAnimator?.duration = value / 5L
        secondAnimator?.duration = value / 5L
        thirdAnimator?.duration = value / 5L
        fourthAnimator?.duration = value / 5L
        fifthAnimator?.duration = value / 5L
        cycleDuration = value
    }

    /**
     * Sets the `TextView`'s seconds text according to the device`s language.
     */
    fun getSeconds(): Int {
        return seconds
    }

    fun setSeconds(value: Int) {
        val textView = findViewById<TextView>(R.id.tv_seconds)
        textView.text = context.resources.getQuantityString(
            R.plurals.quick_seek_x_second, value, value
        )
        seconds = value
    }

    /**
     * Mirrors the triangles depending on what kind of type should be used (forward/rewind).
     */
    fun isForward(): Boolean {
        return isForward
    }

    fun setForward(value: Boolean) {
        val linearLayout = findViewById<LinearLayout>(R.id.triangle_container)
        linearLayout.rotation = if (value) 0f else 180f
        isForward = value
    }

    fun getTextView(): TextView {
        return findViewById<View>(R.id.tv_seconds) as TextView
    }

    fun getIcon(): Int {
        return icon
    }

    fun setIcon(value: Int) {
        if (value > 0) {
            (findViewById<View>(R.id.icon_1) as ImageView).setImageResource(value)
            (findViewById<View>(R.id.icon_2) as ImageView).setImageResource(value)
            (findViewById<View>(R.id.icon_3) as ImageView).setImageResource(value)
        }
        icon = value
    }

    /**
     * Starts the triangle animation
     */
    fun start() {
        stop()
        animate = true
        firstAnimator?.start()
    }

    /**
     * Stops the triangle animation
     */
    fun stop() {
        animate = false
        firstAnimator?.cancel()
        secondAnimator?.cancel()
        thirdAnimator?.cancel()
        fourthAnimator?.cancel()
        fifthAnimator?.cancel()
        reset()
    }

    private fun reset() {
        findViewById<View>(R.id.icon_1).alpha = 0f
        findViewById<View>(R.id.icon_2).alpha = 0f
        findViewById<View>(R.id.icon_3).alpha = 0f
    }

    private inner class CustomValueAnimator(
        start: Runnable,
        update: Consumer<Float?>,
        end: Runnable
    ) : ValueAnimator() {
        init {
            duration = getCycleDuration() / 5L
            setFloatValues(0f, 1f)
            addUpdateListener { animation -> update.accept(animation.animatedValue as Float) }
            addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    start.run()
                }

                override fun onAnimationEnd(animation: Animator) {
                    end.run()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
    }
}