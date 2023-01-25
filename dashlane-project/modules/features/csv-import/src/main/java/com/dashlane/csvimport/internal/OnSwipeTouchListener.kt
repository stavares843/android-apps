package com.dashlane.csvimport.internal

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs



internal abstract class OnSwipeTouchListener(
    context: Context
) : View.OnTouchListener {
    private val detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent) = true

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ) = try {
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (abs(diffX) > abs(diffY) &&
                abs(diffX) > SWIPE_THRESHOLD &&
                abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
            ) {
                if (diffX > 0) {
                    onSwipeRight()
                } else {
                    onSwipeLeft()
                }
            } else {
                false
            }
        } catch (_: Throwable) {
            false
        }
    })

    final override fun onTouch(v: View?, event: MotionEvent) = detector.onTouchEvent(event)

    abstract fun onSwipeRight(): Boolean

    abstract fun onSwipeLeft(): Boolean

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }
}