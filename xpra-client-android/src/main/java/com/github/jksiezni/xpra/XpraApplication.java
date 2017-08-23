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

package com.github.jksiezni.xpra;

import android.app.Application;
import android.content.Context;

import com.github.jksiezni.xpra.db.DatabaseHelper;
import com.j256.ormlite.android.apptools.OpenHelperManager;


public class XpraApplication extends Application {

    private DatabaseHelper database;

    @Override
    public void onCreate() {
        super.onCreate();
        this.database = OpenHelperManager.getHelper(this, DatabaseHelper.class);
    }

    public static XpraApplication getInstance(Context context) {
        return (XpraApplication) context.getApplicationContext();
    }

    public static DatabaseHelper getDatabase(Context context) {
        return getInstance(context).database;
    }
}
