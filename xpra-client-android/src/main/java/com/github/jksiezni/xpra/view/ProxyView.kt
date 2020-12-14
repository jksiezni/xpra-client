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
import android.graphics.SurfaceTexture
import android.view.*
import com.github.jksiezni.xpra.client.AndroidXpraKeyboard
import com.github.jksiezni.xpra.client.AndroidXpraWindow
import timber.log.Timber
import kotlin.math.max

/**
 * A proxy view for Xpra windows, dialogs, popups, menus, etc.
 *
 * Created in code only.
 */
@SuppressLint("ViewConstructor")
class ProxyView(context: Context, val window: AndroidXpraWindow) : TextureView(context) {

    init {
        surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                window.show(surface, width, height)
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
                Timber.v("onSurfaceTextureSizeChanged(): windowId=${window.id}, ${width}x${height}")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                window.hide(surface)
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
                // do nothing
            }
        }
        setOnTouchListener(TouchHandler())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (window.hasParent()) {
            setMeasuredDimension((window.width*window.scale).toInt(), (window.height*window.scale).toInt())
        } else {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
            val minWidth = (window.minimumWidth * window.scale).toInt()
            val minHeight = (window.minimumHeight * window.scale).toInt()

            // adjust to size constraints
            if (minWidth > measuredWidth || minHeight > measuredHeight) {
                setMeasuredDimension(max(minWidth, measuredWidth), max(minHeight, measuredHeight))
            }
        }
    }

    override fun getLayoutParams(): ViewGroup.LayoutParams? {
        val params = super.getLayoutParams()
        if (params is ViewGroup.MarginLayoutParams) {
            if (window.hasParent()) {
                params.leftMargin = window.scaledX
                params.topMargin = window.scaledY
            }
        }
        return params
    }

    inner class TouchHandler : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            event.offsetLocation(x, y)
            val scale = window.scale
            val x = (max(event.x, 0f) / scale).toInt()
            val y = (max(event.y, 0f) / scale).toInt()
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    window.mouseAction(1, true, x, y)
                MotionEvent.ACTION_MOVE -> window.mouseAction(1, true, x, y)
                MotionEvent.ACTION_UP -> window.mouseAction(1, false, x, y)
            }
            return true
        }
    }

    inner class KeyHandler : OnKeyListener {
        override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
            Timber.v("onKey(%d, %s)", keyCode, event)
            if (event.isSystem) {
                Timber.v("isSystem event")
                return false
            }
            Timber.v("char=%d", event.unicodeChar)
            when (event.action) {
                KeyEvent.ACTION_DOWN -> window.keyboardAction(keyCode, AndroidXpraKeyboard.getUnicodeName(keyCode), true)
                KeyEvent.ACTION_UP -> window.keyboardAction(keyCode, AndroidXpraKeyboard.getUnicodeName(keyCode), false)
                else -> {
                }
            }
            return true
        }
    }

}
