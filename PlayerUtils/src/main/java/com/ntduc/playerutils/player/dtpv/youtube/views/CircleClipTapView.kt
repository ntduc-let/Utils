package com.ntduc.playerutils.player.dtpv.youtube.views

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.ntduc.playerutils.R

/**
 * View class
 *
 * Draws a arc shape and provides a circle scaling animation.
 * Used by [YouTubeOverlay][com.github.vkay94.dtpv.youtube.YouTubeOverlay].
 */
class CircleClipTapView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val backgroundPaint: Paint = Paint()
    private val circlePaint: Paint = Paint()
    private var widthPx: Int
    private var heightPx: Int

    // Background
    private val shapePath: Path
    private var isLeft: Boolean

    // Circle
    private var cX: Float
    private var cY: Float
    private var currentRadius: Float
    private var minRadius: Int
    private var maxRadius: Int

    // Animation
    private var valueAnimator: ValueAnimator?
    private var forceReset: Boolean
    private var arcSize: Float

    /*
        Getter and setter
     */
    var performAtEnd: Runnable

    init {
        widthPx = 0
        heightPx = 0

        // Background
        shapePath = Path()
        isLeft = true
        cX = 0f
        cY = 0f
        currentRadius = 0f
        minRadius = 0
        maxRadius = 0
        valueAnimator = null
        forceReset = false
        backgroundPaint.style = Paint.Style.FILL
        backgroundPaint.isAntiAlias = true
        backgroundPaint.color = ContextCompat.getColor(
            context,
            R.color.dtpv_yt_background_circle_color
        )
        circlePaint.style = Paint.Style.FILL
        circlePaint.isAntiAlias = true
        circlePaint.color =
            ContextCompat.getColor(context, R.color.dtpv_yt_tap_circle_color)

        // Pre-configuations depending on device display metrics
        val dm = context.resources.displayMetrics
        widthPx = dm.widthPixels
        heightPx = dm.heightPixels
        minRadius = (30f * dm.density).toInt()
        maxRadius = (400f * dm.density).toInt()
        updatePathShape()
        valueAnimator = circleAnimator
        arcSize = 80f
        performAtEnd = Runnable { }
    }

    fun getArcSize(): Float {
        return arcSize
    }

    fun setArcSize(value: Float) {
        arcSize = value
        updatePathShape()
    }

    var circleBackgroundColor: Int
        get() = backgroundPaint.color
        set(value) {
            backgroundPaint.color = value
        }
    var circleColor: Int
        get() = circlePaint.color
        set(value) {
            circlePaint.color = value
        }
    var animationDuration: Long
        get() = if (valueAnimator != null) valueAnimator!!.duration else 650L
        set(value) {
            circleAnimator!!.duration = value
        }

    /*
       Methods
    */
    /*
        Circle
     */
    fun updatePosition(x: Float, y: Float) {
        cX = x
        cY = y
        val newIsLeft = x <= (resources.displayMetrics.widthPixels / 2).toFloat()
        if (isLeft != newIsLeft) {
            isLeft = newIsLeft
            updatePathShape()
        }
    }

    private fun invalidateWithCurrentRadius(factor: Float) {
        currentRadius = minRadius.toFloat() + (maxRadius - minRadius).toFloat() * factor
        invalidate()
    }

    /*
        Background
     */
    private fun updatePathShape() {
        val halfWidth = widthPx.toFloat() * 0.5f
        shapePath.reset()
        val w = if (isLeft) 0.0f else widthPx.toFloat()
        val f = if (isLeft) 1 else -1
        shapePath.moveTo(w, 0.0f)
        shapePath.lineTo(f.toFloat() * (halfWidth - arcSize) + w, 0.0f)
        shapePath.quadTo(
            f.toFloat() * (halfWidth + arcSize) + w,
            heightPx.toFloat() / 2f,
            f.toFloat() * (halfWidth - arcSize) + w, heightPx.toFloat()
        )
        shapePath.lineTo(w, heightPx.toFloat())
        shapePath.close()
        invalidate()
    }

    /*
        Animation
     */
    private val circleAnimator: ValueAnimator?
        get() {
            if (valueAnimator == null) {
                valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
                valueAnimator?.duration = animationDuration
                valueAnimator?.addUpdateListener { animation ->
                    invalidateWithCurrentRadius(
                        animation.animatedValue as Float
                    )
                }
                valueAnimator?.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        visibility = VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        if (!forceReset) performAtEnd.run()
                    }

                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                })
            }
            return valueAnimator
        }

    fun resetAnimation(body: Runnable) {
        forceReset = true
        circleAnimator!!.end()
        body.run()
        forceReset = false
        circleAnimator!!.start()
    }

    fun endAnimation() {
        circleAnimator!!.end()
    }

    /*
        Others: Drawing and Measurements
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        widthPx = w
        heightPx = h
        updatePathShape()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Background
        canvas?.clipPath(shapePath)
        canvas?.drawPath(shapePath, backgroundPaint)

        // Circle
        canvas?.drawCircle(cX, cY, currentRadius, circlePaint)
    }
}