/*
 * Copyright (C) 2017 Jakub Ksiezniak
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

package com.github.jksiezni.xpra.ssh;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog;

import com.github.jksiezni.xpra.UiTask;

class YesNoAskTask extends UiTask<String, Boolean> {

    private final Context context;

    public YesNoAskTask(Context context) {
        this.context = context;
    }

    @Override
    protected void doOnUIThread(String... params) {
        new YesNoDialog(context, params[0]).show();
    }

    public class YesNoDialog extends AlertDialog.Builder {

        public YesNoDialog(Context ctx, String message) {
            super(ctx);
            setMessage(message);
            setPositiveButton("YES", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postResult(true);
                }
            });
            setNegativeButton("NO", new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postResult(false);
                }
            });
            setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    postResult(false);
                }
            });
        }

    }
}

