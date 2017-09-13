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

import java.util.Iterator;
import java.util.Map;

/**
 * @author Jakub Księżniak
 *
 */
public class NewWindow extends WindowPacket {

	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected WindowMetadata metadata;
	
	public NewWindow() {
		super("new-window");
	}

	protected NewWindow(String type) {
    super(type);
  }
	
	@SuppressWarnings("unchecked")
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		x = asInt(iter.next());
		y = asInt(iter.next());
		width = asInt(iter.next());
		height = asInt(iter.next());
		metadata = new WindowMetadata(windowId, (Map<String, Object>) iter.next());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() +
				"( id=" + windowId + ", " + x + 'x' + y + ':' + width + 'x' + height + ", " + metadata + ')';
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

	public WindowMetadata getMetadata() {
		return metadata;
	}

	public boolean isOverrideRedirect() {
		return metadata.getAsBoolean("override-redirect");
	}
}
