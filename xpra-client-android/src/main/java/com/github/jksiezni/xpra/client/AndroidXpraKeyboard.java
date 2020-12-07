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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import xpra.client.XpraKeyboard;

import android.util.SparseArray;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;


public class AndroidXpraKeyboard implements XpraKeyboard {

    private static final SparseArray<String> keymap = new SparseArray<>();

    static {
        final KeyCharacterMap characterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
        for (int i = KeyEvent.KEYCODE_0; i <= KeyEvent.KEYCODE_9; ++i) {
            mapKeycode(i, characterMap);
        }
        for (int i = KeyEvent.KEYCODE_A; i <= KeyEvent.KEYCODE_Z; ++i) {
            mapKeycode(i, characterMap);
        }
        keymap.put(KeyEvent.KEYCODE_DEL, "BackSpace");
        keymap.put(KeyEvent.KEYCODE_ENTER, "Return");
    }

    private static void mapKeycode(int keyCode, KeyCharacterMap characterMap) {
        int c = characterMap.get(keyCode, 0);
        keymap.put(keyCode, String.format("U%s", Integer.toHexString(c).toUpperCase(Locale.US)));
    }

    public static String getUnicodeName(int keycode) {
        return keymap.get(keycode);
    }

    /* (non-Javadoc)
     * @see xpra.client.XpraKeyboard#getLocale()
     */
    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    /* (non-Javadoc)
     * @see xpra.client.XpraKeyboard#getKeycodes()
     */
    @Override
    public List<KeyDesc> getKeycodes() {
        List<KeyDesc> keys = new ArrayList<>();
        for (int i = 0; i < keymap.size(); ++i) {
            int key = keymap.keyAt(i);
            String val = keymap.valueAt(i);
            keys.add(new KeyDesc(key, val));
        }
        return keys;
    }

}
