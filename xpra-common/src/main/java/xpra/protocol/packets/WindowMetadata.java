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

package xpra.protocol.packets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class WindowMetadata extends WindowPacket {
    public static final int NO_PARENT = -1;

    private static final String META_TITLE = "title";
    private static final String META_ICON = "icon";
    private static final String META_WINDOW_TYPE = "window-type";

    public static final String WINDOW_TYPE_NORMAL = "NORMAL";
    public static final String WINDOW_TYPE_POPUP_MENU = "POPUP_MENU";
    public static final String WINDOW_TYPE_DROPDOWN_MENU = "DROPDOWN_MENU";

    private final Map<String, Object> meta;

    public WindowMetadata() {
        this(0, new HashMap<>());
    }

    public WindowMetadata(int windowId, Map<String, Object> meta) {
        super("window-metadata", windowId);
        this.meta = meta;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deserialize(Iterator<Object> iter) {
        super.deserialize(iter);
        meta.putAll((Map<String, Object>) iter.next());
    }

    public int getWindowId() {
        return windowId;
    }

    public String getAsString(String key) {
        return asString(meta.get(key));
    }

    public boolean getAsBoolean(String key) {
        Object value = meta.get(key);
        return asBoolean(value);
    }

    public WindowIcon getIcon() {
        List<?> iconlist = (List<?>) meta.get(META_ICON);
        if (iconlist != null) {
            WindowIcon icon = new WindowIcon(windowId);
            icon.readLocal(iconlist.iterator());
            return icon;
        }
        return null;
    }

    @Override
    public String toString() {
        TreeMap<String, Object> m = new TreeMap<>();
        for (Entry<String, Object> e : meta.entrySet()) {
            if (e.getValue() instanceof List) {
                @SuppressWarnings("unchecked")
                List<Object> list = new ArrayList<>((List<Object>) e.getValue());
                for (int i = 0; i < list.size(); ++i) {
                    if (list.get(i) instanceof byte[]) {
                        list.set(i, asString(list.get(i)));
                    }
                }
                m.put(e.getKey(), list);
            } else if (e.getValue() instanceof byte[]) {
                m.put(e.getKey(), asString(e.getValue()));
            } else {
                m.put(e.getKey(), e.getValue());
            }
        }
        return m.toString();
    }

    public int getParentId() {
        final Object value = meta.get("transient-for");
        if (value != null) {
            return asInt(value);
        }
        return NO_PARENT;
    }

    public Integer getAsInt(String key) {
        final Object value = meta.get(key);
        if (value != null) {
            return asInt(value);
        }
        return null;
    }

    public Set<String> getWindowTypes() {
        final Object value = meta.get(META_WINDOW_TYPE);
        if (value != null) {
            List<String> list = asStringList(value);
            return Collections.unmodifiableSet(new HashSet<>(list));
        }
        return Collections.emptySet();
    }

    public String getTitle() {
        return getAsString(META_TITLE);
    }

}
