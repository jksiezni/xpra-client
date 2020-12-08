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
 * Created in code only.
 */
@SuppressLint("ViewConstructor")
class ProxyWindowView(context: Context, val window: AndroidXpraWindow) : TextureView(context) {

    init {
        surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                Timber.tag("ProxyWindowView(${window.id})").v("onSurfaceTextureAvailable(): $width x $height")
                window.show(surface, width, height)
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
                Timber.tag("ProxyWindowView(${window.id})").v("onSurfaceTextureSizeChanged")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Timber.tag("ProxyWindowView(${window.id})").v("onSurfaceTextureDestroyed")
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
        }
    }

    override fun getLayoutParams(): ViewGroup.LayoutParams? {
        val params = super.getLayoutParams()
        if (params is ViewGroup.MarginLayoutParams) {
            params.leftMargin = (window.x * window.scale).toInt()
            params.topMargin = (window.y * window.scale).toInt()
        }
        return params
    }

    inner class TouchHandler : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            Timber.tag("TouchHandler_${window.id}").v("onTouch() evt=%s", event)
            event.offsetLocation(x, y)
            val scale = window.scale
            val x = (max(event.x, 0f) / scale).toInt()
            val y = (max(event.y, 0f) / scale).toInt()
            Timber.tag("TouchHandler_${window.id}").v("onTouch(): point=%d,%d", x, y)
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
