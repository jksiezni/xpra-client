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

import java.util.Collection;
import java.util.Iterator;

public abstract class WindowPacket extends xpra.protocol.IOPacket {

    protected int windowId;

    protected WindowPacket(String type) {
        super(type);
    }

    protected WindowPacket(String type, int wid) {
        super(type);
        this.windowId = wid;
    }

    public int getWindowId() {
        return windowId;
    }

    @Override
    protected void serialize(Collection<Object> elems) {
        elems.add(windowId);
    }

    @Override
    protected void deserialize(Iterator<Object> iter) {
        super.deserialize(iter);
        windowId = asInt(iter.next());
    }

    @Override
    public String toString() {
        return type + "{ windowId=" + windowId + "}";
    }
}