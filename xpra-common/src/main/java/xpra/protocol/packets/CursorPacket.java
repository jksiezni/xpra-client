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

import java.util.Iterator;

public class CursorPacket extends xpra.protocol.Packet {

    private boolean empty;

    public String encoding;
    public int x;
    public int y;
    public int width;
    public int height;
    public int xHotspot;
    public int yHotspot;
    public int serial;
    public byte[] pixels;

    public String name;

    @Override
    public void deserialize(Iterator<Object> iter) {
        super.deserialize(iter);
        Object first = iter.next();
        if (first instanceof byte[]) {
            encoding = asString(first);
            if (encoding.isEmpty()) {
                empty = true;
                return; // empty cursor
            }
            first = iter.next();
        } else {
            // old xpra server sent only raw cursor data
            encoding = "raw";
        }
        empty = false;
        x = asInt(first);
        y = asInt(iter.next());
        width = asInt(iter.next());
        height = asInt(iter.next());
        xHotspot = asInt(iter.next());
        yHotspot = asInt(iter.next());
        serial = asInt(iter.next());
        pixels = asByteArray(iter.next());

        if (iter.hasNext()) {
            // named cursor
            name = asString(iter.next());
        }
    }

    public boolean isEmpty() {
        return empty;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(getClass().getSimpleName());
        builder.append(": ");
        if (isEmpty()) {
            builder.append("empty");
        } else {
            builder.append("name=").append(name);
            builder.append(" pixels.length=").append(pixels.length);
        }
        return builder.toString();
    }
}
