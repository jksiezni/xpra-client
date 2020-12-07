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

import android.opengl.EGLContext
import android.os.HandlerThread
import android.os.Process
import com.android.grafika.gles.EglCore
import timber.log.Timber

abstract class GLThread(threadName: String = "GLThread", sharedContext: EGLContext? = null, priority: Int = Process.THREAD_PRIORITY_DISPLAY)
    : HandlerThread(threadName, priority) {

    protected val eglCore by lazy { EglCore(sharedContext, EglCore.FLAG_TRY_GLES3) }

    var errorHandler: (java.lang.Exception) -> Unit = { throw it }

    protected abstract fun onSetupGL(eglCore: EglCore)

    protected abstract fun onDestroyGL(eglCore: EglCore)

    override fun onLooperPrepared() {
        onSetupGL(eglCore)
    }

    override fun run() {
        Timber.tag(name).v("Start render thread!")
        try {
            super.run()
            onDestroyGL(eglCore)
            eglCore.release()
        } catch (e: Exception) {
            errorHandler(e)
        }
    }

}
