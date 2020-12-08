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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLUtils
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import com.android.grafika.gles.*
import timber.log.Timber
import xpra.protocol.PictureEncoding
import xpra.protocol.packets.DrawPacket
import java.nio.ByteBuffer

/**
 *
 */
class GLComposer(private val callback: ComposeCallback) : GLThread() {

    private val drawTargets: MutableMap<Int, GLDrawTarget> = mutableMapOf()

    private val baseSurface: EglSurfaceBase by lazy {
        if (eglCore.supportsSurfacelessContext()) {
            return@lazy EglSurfaceBase(eglCore)
        } else {
            return@lazy OffscreenSurface(eglCore, 2, 2)
        }
    }

    private val handler: Handler by lazyHandler(this) { msg ->
        when (msg.what) {
            MSG_CREATE_DRAW_TARGET -> {
                val windowId = msg.arg1
                Timber.v("MSG_CREATE_DRAW_TARGET $windowId")
                drawTargets[windowId] = GLDrawTarget(baseSurface, frameRect.createTextureObject())
            }
            MSG_REMOVE_DRAW_TARGET -> {
                val windowId = msg.arg1
                Timber.v("MSG_REMOVE_DRAW_TARGET $windowId")
                baseSurface.makeCurrent()
                drawTargets.remove(windowId)
            }
            MSG_ADD_SURFACE_TEX -> {
                val windowId = msg.arg1
                val surfaceTexture = msg.obj as SurfaceTexture
                Timber.v("MSG_ADD_SURFACE_TEX $windowId")
                drawTargets[windowId]?.let {
                    it.setTarget(WindowSurface(eglCore, surfaceTexture))
                    it.makeCurrent()
                    render(it)
                }

            }
            MSG_DEL_SURFACE_TEX -> {
                val windowId = msg.arg1
                val surfaceTexture = msg.obj as SurfaceTexture
                Timber.v("MSG_DEL_SURFACE_TEX $windowId")
                baseSurface.makeCurrent()
                drawTargets[windowId]?.setTarget(baseSurface)
                surfaceTexture.release()
            }
            MSG_DRAW_PACKET -> {
                val packet = msg.obj as DrawPacket
                composePacket(packet)
            }
        }
        true
    }

    private lateinit var frameRect: FullFrameRect

    init {
        start()
    }

    override fun onSetupGL(eglCore: EglCore) {
        baseSurface.makeCurrent()
        val program = Texture2dProgram.create(Texture2dProgram.ProgramType.TEXTURE_2D)
        frameRect = FullFrameRect(program)
    }

    override fun onDestroyGL(eglCore: EglCore) {
        eglCore.makeNothingCurrent()
        baseSurface.releaseEglSurface()
    }

    fun createDrawingTarget(windowId: Int) {
        handler.obtainMessage(MSG_CREATE_DRAW_TARGET, windowId, 0).sendToTarget()
    }

    fun destroyDrawingTarget(windowId: Int) {
        handler.obtainMessage(MSG_REMOVE_DRAW_TARGET, windowId, 0).sendToTarget()
    }

    fun queueToDraw(packet: DrawPacket) {
        handler.obtainMessage(MSG_DRAW_PACKET, packet).sendToTarget()
    }

    fun addSurface(windowId: Int, surfaceTexture: SurfaceTexture) {
        handler.obtainMessage(MSG_ADD_SURFACE_TEX, windowId, 0, surfaceTexture).sendToTarget()
    }

    fun removeSurface(windowId: Int, surfaceTexture: SurfaceTexture) {
        handler.obtainMessage(MSG_DEL_SURFACE_TEX, windowId, 0, surfaceTexture).sendToTarget()
    }

    private fun composePacket(packet: DrawPacket) {
        val glWindow = drawTargets[packet.windowId]
        if (glWindow != null) {
            // process packet
            val startTime = SystemClock.uptimeMillis()
            glWindow.makeCurrent()
            glWindow.validateTextureSize(packet.x + packet.w, packet.y + packet.h)
            composeImage(glWindow.texture, packet)
            render(glWindow)
            callback.onComposed(packet, SystemClock.uptimeMillis() - startTime)
        } else {
            Timber.w("No surface to compose a drawing for window id=%d", packet.windowId)
        }
    }

    private fun render(glDrawTarget: GLDrawTarget) {
        if (!glDrawTarget.isEglSurface(baseSurface)) {
            GlUtil.checkGlError("startRender")
            frameRect.drawFrame(glDrawTarget.texture, GlUtil.IDENTITY_MATRIX)
            GlUtil.checkGlError("endRender")
            glDrawTarget.swapBuffers()
        }
    }

    private fun composeImage(tex: Int, packet: DrawPacket) {
        when (packet.encoding) {
            PictureEncoding.png, PictureEncoding.pngL, PictureEncoding.pngP, PictureEncoding.jpeg -> {
                val bitmap = BitmapFactory.decodeByteArray(packet.data, 0, packet.data.size)
                composeBitmap(tex, bitmap, packet.x, packet.y)
                bitmap.recycle()
            }
            PictureEncoding.rgb24 -> {
                composeRGB(tex, packet.readPixels(), packet.x, packet.y, packet.w, packet.h)
            }
            PictureEncoding.rgb32 -> {
                composeRGBA(tex, packet.readPixels(), packet.x, packet.y, packet.w, packet.h)
            }
            else -> Timber.e("Unable to draw: %s", packet.encoding)
        }
    }

    private fun composeRGB(tex: Int, pixels: ByteArray, x: Int, y: Int, width: Int, height: Int) {
        val buffer = ByteBuffer.wrap(pixels)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex)
        GlUtil.checkGlError("glBindTexture")
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1) // rgb24 data is 3-byte aligned, but GL allows only 1-byte align
        GlUtil.checkGlError("glPixelStorei")
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, x, y, width, height, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, buffer)
        GlUtil.checkGlError("texSubImage2D")
        GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 4)
    }

    private fun composeRGBA(tex: Int, pixels: ByteArray, x: Int, y: Int, width: Int, height: Int) {
        val buffer = ByteBuffer.wrap(pixels)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex)
        GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, x, y, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer)
        GlUtil.checkGlError("texSubImage2D")
    }

    private fun composeBitmap(tex: Int, bitmap: Bitmap, x: Int, y: Int) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex)
        GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, x, y, bitmap)
        GlUtil.checkGlError("texSubImage2D")
    }

    fun interface ComposeCallback {
        fun onComposed(packet: DrawPacket, decodeTime: Long)
    }

    companion object {
        const val MSG_DRAW_PACKET = 1
        const val MSG_ADD_SURFACE_TEX = 2
        const val MSG_DEL_SURFACE_TEX = 3
        const val MSG_CREATE_DRAW_TARGET = 4
        const val MSG_REMOVE_DRAW_TARGET = 5

        private fun lazyHandler(handlerThread: HandlerThread, callback: Handler.Callback) : Lazy<Handler> {
            return lazy { Handler(handlerThread.looper, callback) }
        }
    }
}