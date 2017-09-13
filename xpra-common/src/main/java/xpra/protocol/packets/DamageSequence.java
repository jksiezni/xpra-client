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

import xpra.protocol.IOPacket;

public class DamageSequence extends IOPacket {

	private int packet_sequence;
	private int id;
	private int w;
	private int h;
	private long frametime;

	public DamageSequence(DrawPacket response, long frametime) {
		super("damage-sequence");
		this.packet_sequence = response.packet_sequence;
		this.id = response.getWindowId();
		this.w = response.w;
		this.h = response.h;
		this.frametime = frametime;
	}
	
	@Override
	protected void serialize(Collection<Object> elems) {
		elems.add(packet_sequence);
		elems.add(id);
		elems.add(w);
		elems.add(h);
		elems.add(frametime);
	}

}
