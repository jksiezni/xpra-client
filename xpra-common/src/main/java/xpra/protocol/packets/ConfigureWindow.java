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

package xpra.protocol.packets;

import java.util.Collection;
import java.util.Iterator;

public class ConfigureWindow extends WindowPacket {

	private int x;
	private int y;
	private int width;
	private int height;

	public ConfigureWindow(int windowId, int x, int y, int width, int height) {
		super("configure-window", windowId);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	protected ConfigureWindow(String type) {
		super(type);
	}
	
	@Override
	public void serialize(Collection<Object> elems) {
		super.serialize(elems);
		elems.add(x);
		elems.add(y);
		elems.add(width);
		elems.add(height);
	}
	
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		x = asInt(iter.next());
		y = asInt(iter.next());
		width = asInt(iter.next());
		height = asInt(iter.next());
	}

}
