package com.ntduc.audioplayerutils.player.dtpv

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.ntduc.audioplayerutils.R
import com.ntduc.audioplayerutils.player.CustomStyledPlayerView

/**
 * Custom player class for Double-Tapping listening
 */
class DoubleTapPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CustomStyledPlayerView(context, attrs, defStyleAttr) {
    private val gestureDetector: GestureDetectorCompat
    private val gestureListener: DoubleTapGestureListener
    private var controller: PlayerDoubleTapListener? = null
    private fun getController(): PlayerDoubleTapListener? {
        return gestureListener.controls
    }

    private fun setController(value: PlayerDoubleTapListener?) {
        gestureListener.controls = value
        controller = value
    }

    private var controllerRef: Int

    /**
     * If this field is set to `true` this view will handle double tapping, otherwise it will
     * handle touches the same way as the original [PlayerView][com.google.android.exoplayer2.ui.PlayerView] does
     */
    var isDoubleTapEnabled: Boolean

    /**
     * Time window a double tap is active, so a followed tap is calling a gesture detector
     * method instead of normal tap (see [PlayerView.onTouchEvent])
     */
    private var doubleTapDelay: Long

    init {
        controllerRef = -1
        gestureListener = DoubleTapGestureListener(this)
        gestureDetector = GestureDetectorCompat(context, gestureListener)
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.DoubleTapPlayerView, 0, 0)
            controllerRef =
                a.getResourceId(R.styleable.DoubleTapPlayerView_dtpv_controller, -1)
            a.recycle()
        }
        isDoubleTapEnabled = true
        doubleTapDelay = 700L
    }

    fun getDoubleTapDelay(): Long {
        return gestureListener.doubleTapDelay
    }

    fun setDoubleTapDelay(value: Long) {
        gestureListener.doubleTapDelay = value
        doubleTapDelay = value
    }

    /**
     * Sets the [PlayerDoubleTapListener] which handles the gesture callbacks.
     *
     * Primarily used for [YouTubeOverlay][com.github.vkay94.dtpv.youtube.YouTubeOverlay]
     */
    fun controller(controller: PlayerDoubleTapListener): DoubleTapPlayerView {
        setController(controller)
        return this
    }

    /**
     * Returns the current state of double tapping.
     */
    val isInDoubleTapMode: Boolean
        get() = gestureListener.isDoubleTapping

    /**
     * Resets the timeout to keep in double tap mode.
     *
     * Called once in [PlayerDoubleTapListener.onDoubleTapStarted]. Needs to be called
     * from outside if the double tap is customized / overridden to detect ongoing taps
     */
    fun keepInDoubleTapMode() {
        gestureListener.keepInDoubleTapMode()
    }

    /**
     * Cancels double tap mode instantly by calling [PlayerDoubleTapListener.onDoubleTapFinished]
     */
    fun cancelInDoubleTapMode() {
        gestureListener.cancelInDoubleTapMode()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isDoubleTapEnabled) {
            val consumed = gestureDetector.onTouchEvent(ev)

            // Do not trigger original behavior when double tapping
            // otherwise the controller would show/hide - it would flack
            return if (!consumed) super.onTouchEvent(ev) else true
        }
        return super.onTouchEvent(ev)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // If the PlayerView is set by XML then call the corresponding setter method
        if (controllerRef != -1) {
            try {
                val view = (parent as View).findViewById<View>(
                    controllerRef
                )
                if (view is PlayerDoubleTapListener) {
                    controller(view as PlayerDoubleTapListener)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(
                    "DoubleTapPlayerView",
                    "controllerRef is either invalid or not PlayerDoubleTapListener: \${e.message}"
                )
            }
        }
    }

    /**
     * Gesture Listener for double tapping
     *
     * For more information which methods are called in certain situations look for
     * [GestureDetector.onTouchEvent][android.view.GestureDetector.onTouchEvent],
     * especially for ACTION_DOWN and ACTION_UP
     */
    private class DoubleTapGestureListener(private val rootView: CustomStyledPlayerView) :
        GestureDetector.SimpleOnGestureListener() {
        private val mHandler: Handler = Handler(Looper.getMainLooper())
        private val mRunnable: Runnable = Runnable {
            if (DEBUG) Log.d(TAG, "Runnable called")
            isDoubleTapping = false
            this@DoubleTapGestureListener.isDoubleTapping = false
            if (controls != null) controls!!.onDoubleTapFinished()
        }
        var controls: PlayerDoubleTapListener? = null
        var isDoubleTapping = false
        var doubleTapDelay: Long = 650L

        /**
         * Resets the timeout to keep in double tap mode.
         *
         * Called once in [PlayerDoubleTapListener.onDoubleTapStarted]. Needs to be called
         * from outside if the double tap is customized / overridden to detect ongoing taps
         */
        fun keepInDoubleTapMode() {
            isDoubleTapping = true
            mHandler.removeCallbacks(mRunnable)
            mHandler.postDelayed(mRunnable, doubleTapDelay)
        }

        /**
         * Cancels double tap mode instantly by calling [PlayerDoubleTapListener.onDoubleTapFinished]
         */
        fun cancelInDoubleTapMode() {
            mHandler.removeCallbacks(mRunnable)
            isDoubleTapping = false
            if (controls != null) controls!!.onDoubleTapFinished()
        }

        override fun onDown(e: MotionEvent): Boolean {
            // Used to override the other methods
            if (isDoubleTapping) {
                if (controls != null) controls!!.onDoubleTapProgressDown(e.x, e.y)
                return true
            }
            return super.onDown(e)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (isDoubleTapping) {
                if (DEBUG) Log.d(TAG, "onSingleTapUp: isDoubleTapping = true")
                if (controls != null) controls!!.onDoubleTapProgressUp(e.x, e.y)
                return true
            }
            return super.onSingleTapUp(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            // Ignore this event if double tapping is still active
            // Return true needed because this method is also called if you tap e.g. three times
            // in a row, therefore the controller would appear since the original behavior is
            // to hide and show on single tap
            if (isDoubleTapping) return true
            if (DEBUG) Log.d(TAG, "onSingleTapConfirmed: isDoubleTap = false")
            //return rootView.performClick()
            return rootView.tap()
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // First tap (ACTION_DOWN) of both taps
            if (DEBUG) Log.d(TAG, "onDoubleTap")
            if (!isDoubleTapping) {
                isDoubleTapping = true
                keepInDoubleTapMode()
                if (controls != null) controls!!.onDoubleTapStarted(e.x, e.y)
            }
            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            // Second tap (ACTION_UP) of both taps
            if (e.actionMasked == MotionEvent.ACTION_UP && isDoubleTapping) {
                if (DEBUG) Log.d(TAG, "onDoubleTapEvent, ACTION_UP")
                if (controls != null) controls!!.onDoubleTapProgressUp(e.x, e.y)
                return true
            }
            return super.onDoubleTapEvent(e)
        }

        companion object {
            private const val TAG = ".DTGListener"
            private const val DEBUG = false
        }
    }
}