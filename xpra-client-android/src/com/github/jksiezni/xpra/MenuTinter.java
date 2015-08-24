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
