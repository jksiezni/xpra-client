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
import android.view.Gravity;
import android.view.TextureView;
import android.widget.PopupWindow;

import xpra.protocol.packets.NewWindow;

/**
 *
 */

public class PopupXpraWindow extends AndroidXpraWindow {

    private final PopupWindow popup;
    private final AndroidXpraWindow parentWindow;

    PopupXpraWindow(NewWindow wndPacket, Context context, AndroidXpraWindow window) {
        super(wndPacket, context);
        parentWindow = window;
        TextureView textureView = new TextureView(context);
        setTargetView(textureView);
        popup = new PopupWindow(textureView, wndPacket.getWidth(), wndPacket.getHeight());

    }

    @Override
    protected void onStart(NewWindow wnd) {
        super.onStart(wnd);
        getHandler().post(() -> popup.showAtLocation(parentWindow.getView(), Gravity.TOP | Gravity.LEFT, wnd.getX(), wnd.getY()));
    }

    @Override
    protected void onStop() {
        //popup.dismiss();
    }


    public void close() {
        if (popup.isShowing()) {
            getHandler().post(() -> popup.dismiss());
        }
    }

}
