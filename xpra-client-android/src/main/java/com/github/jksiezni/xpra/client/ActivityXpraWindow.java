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

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import timber.log.Timber;
import xpra.protocol.packets.NewWindow;


public class ActivityXpraWindow extends AndroidXpraWindow {

    ActivityXpraWindow(NewWindow window, Context context) {
        super(window, context);
    }

    @Override
    protected void onStart(NewWindow wndPacket) {
        super.onStart(wndPacket);
        Timber.i("onStart() windowId=%s", getId());
//		final LayoutParams params = wndPacket.isOverrideRedirect() ?
//				buildExactParams(wndPacket) :
//				buildFullscreenParams(wndPacket);
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.AppTask> tasks = am.getAppTasks();
        Intent intent = Intents.createXpraIntent(context, getId());
        for (ActivityManager.AppTask task : tasks) {
            if (intent.filterEquals(task.getTaskInfo().baseIntent)) {
                task.moveToFront();
                return;
            }
        }
        context.startActivity(intent);
    }
}
