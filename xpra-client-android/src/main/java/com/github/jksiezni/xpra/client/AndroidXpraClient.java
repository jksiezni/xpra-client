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

package com.github.jksiezni.xpra.client;

import android.content.Context;
import android.util.DisplayMetrics;

import java.util.Collection;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import xpra.client.XpraClient;
import xpra.client.XpraWindow;
import xpra.protocol.PictureEncoding;
import xpra.protocol.packets.CursorPacket;
import xpra.protocol.packets.NewWindow;

public class AndroidXpraClient extends XpraClient {

    private static final PictureEncoding[] PICTURE_ENCODINGS = {
        PictureEncoding.png,
        PictureEncoding.pngL,
        PictureEncoding.pngP,
        PictureEncoding.jpeg,
        PictureEncoding.rgb24,
        PictureEncoding.rgb32
    };

    private final Context context;

    private MutableLiveData<Collection<XpraWindow>> windowsLiveData = new MutableLiveData<>();


    public AndroidXpraClient(Context context) {
        super(0, 0, PICTURE_ENCODINGS, new AndroidXpraKeyboard());
        this.context = context;

        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        setDesktopSize(dm.widthPixels, dm.heightPixels);
        //setDpi(dm.densityDpi, (int) dm.xdpi, (int) dm.ydpi);
    }

    @Override
    protected XpraWindow onCreateWindow(NewWindow wnd) {
        return new AndroidXpraWindow(wnd, context);
//        if (wnd.isOverrideRedirect()) {
//            AndroidXpraWindow window = getWindow(wnd.getMetadata().getParentId());
//            return new PopupXpraWindow(wnd, context, window);
//        } else {
//            return new ActivityXpraWindow(wnd, context);
//        }
    }

    @Override
    protected void onWindowStarted(XpraWindow window) {
        super.onWindowStarted(window);
        windowsLiveData.postValue(getWindows());
    }

    @Override
    protected void onWindowMetadataUpdated(XpraWindow window) {
        super.onWindowMetadataUpdated(window);
        windowsLiveData.postValue(getWindows());
    }

    @Override
    protected void onDestroyWindow(XpraWindow window) {
        if (window instanceof PopupXpraWindow) {
            PopupXpraWindow popup = (PopupXpraWindow) window;
            popup.close();
        }
    }

    @Override
    protected void onCursorUpdate(CursorPacket cursorPacket) {
        super.onCursorUpdate(cursorPacket);
    }

    @Override
    public AndroidXpraWindow getWindow(int windowId) {
        return (AndroidXpraWindow) super.getWindow(windowId);
    }

    public LiveData<Collection<XpraWindow>> getWindowsLiveData() {
        return windowsLiveData;
    }
}
