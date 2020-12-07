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
package com.github.jksiezni.xpra.client

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.jksiezni.xpra.gl.GLComposer
import timber.log.Timber
import xpra.client.XpraClient
import xpra.client.XpraWindow
import xpra.protocol.PictureEncoding
import xpra.protocol.packets.DrawPacket
import xpra.protocol.packets.NewWindow

class AndroidXpraClient(private val context: Context) : XpraClient(0, 0, PICTURE_ENCODINGS, AndroidXpraKeyboard()) {

    private val windowsLiveData = MutableLiveData<Collection<XpraWindow>>()
    private val listeners: MutableList<XpraEventListener> = mutableListOf()

    private val composer = GLComposer(this::onDrawFinished)

    init {
        val dm = context.resources.displayMetrics
        setDesktopSize(dm.widthPixels, dm.heightPixels)
    }

    override fun onCreateWindow(wnd: NewWindow, parentWindow: XpraWindow?): XpraWindow {
        return if (parentWindow != null) {
            val parent = getWindow(parentWindow.id)
            AndroidXpraWindow(wnd, context, composer, parent)
        } else {
            AndroidXpraWindow(wnd, context, composer)
        }
    }

    override fun onWindowStarted(window: XpraWindow) {
        super.onWindowStarted(window)
        windowsLiveData.postValue(windows.filter { !it.hasParent() })
        listeners.forEach { it.onWindowCreated(window as AndroidXpraWindow) }
    }

    override fun onWindowMetadataUpdated(window: XpraWindow) {
        super.onWindowMetadataUpdated(window)
        windowsLiveData.postValue(windows.filter { !it.hasParent() })
    }

    override fun onDestroyWindow(window: XpraWindow) {
        super.onDestroyWindow(window)
        val androidXpraWindow = window as AndroidXpraWindow
        androidXpraWindow.release()
        listeners.forEach { it.onWindowLost(androidXpraWindow) }
        windowsLiveData.postValue(windows.filter { !it.hasParent() })
    }

    override fun getWindow(windowId: Int): AndroidXpraWindow? {
        return super.getWindow(windowId) as AndroidXpraWindow?
    }

    fun getWindowsLiveData(): LiveData<Collection<XpraWindow>> {
        return windowsLiveData
    }

    fun addEventListener(listener: XpraEventListener) {
        listeners.add(listener)
    }

    fun removeEventListener(listener: XpraEventListener) {
        listeners.remove(listener)
    }

    private fun onDrawFinished(drawPacket: DrawPacket, decodeTime: Long) {
        Timber.v("onDrawFinished() decodeTime=%d", decodeTime)
        AndroidXpraWindow.sendDamageSequence(sender, drawPacket, decodeTime)
    }

    companion object {
        private val PICTURE_ENCODINGS = arrayOf(
                PictureEncoding.png,
                PictureEncoding.pngL,
                PictureEncoding.pngP,
                PictureEncoding.jpeg,
                PictureEncoding.rgb24,
                PictureEncoding.rgb32
        )
    }
}
