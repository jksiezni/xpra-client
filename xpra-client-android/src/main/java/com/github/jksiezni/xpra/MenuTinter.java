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

import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.Menu;
import android.view.MenuItem;

public class MenuTinter {

    public static void tintMenuIcons(Menu menu, @ColorInt int color) {
        for(int i = 0; i < menu.size(); ++i) {
            final MenuItem item = menu.getItem(i);
            Drawable drawable = item.getIcon();
            if(null != drawable) {
                drawable = DrawableCompat.wrap(drawable);
                DrawableCompat.setTint(drawable, color);
                item.setIcon(drawable);
            }
        }
    }
}
