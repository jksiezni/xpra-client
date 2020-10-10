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

package com.github.jksiezni.xpra.config;

import android.app.Application;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

/**
 *
 */
@Database(entities = {ServerDetails.class}, version = 2, exportSchema = false)
@TypeConverters(ConvertersKt.class)
public abstract class ConfigDatabase extends RoomDatabase {

    public abstract ConnectionDao getConfigs();

    private static ConfigDatabase instance;

    public static void setup(Application app) {
        if (instance == null) {
            instance = Room.databaseBuilder(app, ConfigDatabase.class, "config.db")
                .fallbackToDestructiveMigration()
                .build();
        }
    }

    public static ConfigDatabase getInstance() {
        return instance;
    }
}
