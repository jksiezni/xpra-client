/*
 * Copyright (C) 2020 Jakub Ksiezniak
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.github.jksiezni.xpra.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import android.widget.Scroller
import androidx.core.math.MathUtils
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.children

/**
 *
 */
class WorkspaceView : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private val overflingDistance = ViewConfiguration.get(context).scaledOverflingDistance

    override fun shouldDelayChildPressedState(): Boolean {
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return ev.pointerCount >= 2
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun scrollBy(x: Int, y: Int) {
        val r = getScrollRange()
        val tx = MathUtils.clamp(scrollX + x, r.left, r.right - width)
        val ty = MathUtils.clamp(scrollY + y, r.top, r.bottom - height)
        super.scrollTo(tx, ty)
    }

    private fun getScrollRange(): Rect {
        val temp = Rect()
        return children.fold(Rect()) { acc, view ->
            view.getDrawingRect(temp)
            acc.apply { union(temp) }
        }
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            val oldX: Int = scrollX
            val oldY: Int = scrollY
            val x: Int = scroller.currX
            val y: Int = scroller.currY

            if (oldX != x || oldY != y) {
                val range = getScrollRange()
                overScrollBy(x - oldX, y - oldY, oldX, oldY, range.width(), range.height(),
                        overflingDistance, overflingDistance, false)
                onScrollChanged(scrollX, scrollY, oldX, oldY)
            }

            if (!awakenScrollBars()) {
                // Keep on drawing until the animation has finished.
                postInvalidateOnAnimation()
            }
        }
    }

    override fun onOverScrolled(scrollX: Int, scrollY: Int, clampedX: Boolean, clampedY: Boolean) {
        scrollTo(scrollX, scrollY)
        awakenScrollBars()
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        val range = getScrollRange()
        val viewport = Rect().apply { getDrawingRect(this) }

        var dx = 0
        var dy = 0
        if (range.right < viewport.right) {
            dx = range.right - viewport.right
        }
        if (range.bottom < viewport.bottom) {
            dy = range.bottom - viewport.bottom
        }
        smoothScrollBy(dx, dy)
    }

    fun smoothScrollBy(dx: Int, dy: Int) {
        scroller.startScroll(scrollX, scrollY, dx, dy)
        postInvalidateOnAnimation()
    }

    private val scroller = Scroller(context)

    private val gestureDetector: GestureDetectorCompat = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            scroller.forceFinished(true)
            scrollBy(distanceX.toInt(), distanceY.toInt())
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            val bounds = getScrollRange()
            scroller.forceFinished(true)
            scroller.fling(scrollX, scrollY, -velocityX.toInt(), -velocityY.toInt(),
                    bounds.left, bounds.right-width,
                    bounds.top, bounds.bottom-height)
            postInvalidateOnAnimation()
            return true
        }
    })
}
