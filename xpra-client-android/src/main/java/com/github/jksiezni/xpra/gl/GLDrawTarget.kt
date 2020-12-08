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

package com.github.jksiezni.xpra.gl

import android.opengl.GLES20
import com.android.grafika.gles.EglSurfaceBase
import com.android.grafika.gles.GlUtil
import com.android.grafika.gles.WindowSurface
import timber.log.Timber

/**
 *
 */
internal class GLDrawTarget(
        private var eglSurface: EglSurfaceBase,
        val texture: Int
        ) {

    private var textureWidth: Int = 0
    private var textureHeight: Int = 0


    fun validateTextureSize(width: Int, height: Int) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture)
        if (width > textureWidth || height > textureHeight) {
            Timber.d("create texture ${width}x${height}")
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, width, height, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, null)
            GlUtil.checkGlError("glTexImage2D")
            textureWidth = width
            textureHeight = height
        }
    }

    fun makeCurrent() {
        eglSurface.makeCurrent()
        if (eglSurface is WindowSurface) {
            GLES20.glViewport(0, 0, eglSurface.width, eglSurface.height)
        }
    }

    fun swapBuffers() {
        eglSurface.swapBuffers()
        GlUtil.checkGlError("sswapBuffers")
    }

    fun setTarget(eglSurface: EglSurfaceBase) {
        if (this.eglSurface is WindowSurface) {
            this.eglSurface.releaseEglSurface()
        }
        this.eglSurface = eglSurface
    }

    fun isEglSurface(surface: EglSurfaceBase) : Boolean = eglSurface == surface

}
