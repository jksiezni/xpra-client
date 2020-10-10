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

import xpra.protocol.PictureEncoding;

/**
 * @author Jakub Księżniak
 *
 */
public class WindowIcon extends WindowPacket {

	public int width;
	public int height;
	public PictureEncoding encoding;
	public byte[] data;
	
	public WindowIcon() {
		super("window-icon");
	}
	
	public WindowIcon(int windowId) {
		super("window-icon", windowId);
	}
	
	@Override
	public void deserialize(Iterator<Object> iter) {
		super.deserialize(iter);
		readLocal(iter);
	}
	
	void readLocal(Iterator<?> iter) {
		width = asInt(iter.next());
		height = asInt(iter.next());
		encoding = PictureEncoding.decode(asString(iter.next()));
		data = (byte[]) iter.next();	
	}

}
